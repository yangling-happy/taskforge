package com.taskforge.scheduler.service;

import com.taskforge.common.constant.TaskConstants;
import com.taskforge.common.entity.Task;
import com.taskforge.scheduler.mapper.TaskMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 扫描线程（单线程轮询）：游标分页捞到期 PENDING 任务 ->
 * Redisson 锁防并发 -> MySQL CAS 领取 -> put 到 Redis 就绪队列。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScanService {

    private final TaskMapper taskMapper;
    private final RedissonClient redisson;

    private ScheduledExecutorService executor;

    @PostConstruct
    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "scan-thread");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleWithFixedDelay(this::scanOnce, 1000, 1000, TimeUnit.MILLISECONDS);
        log.info("ScanService started");
    }

    @PreDestroy
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void scanOnce() {
        try {
            LocalDateTime cursorTime = LocalDateTime.of(1970, 1, 1, 0, 0);
            long cursorId = 0;
            while (true) {
                List<Task> tasks = taskMapper.scanPending(cursorTime, cursorId);
                if (tasks.isEmpty()) {
                    return;
                }
                for (Task task : tasks) {
                    tryClaimAndEnqueue(task);
                }
                if (tasks.size() < TaskConstants.SCAN_BATCH_SIZE) {
                    return;
                }
                Task last = tasks.get(tasks.size() - 1);
                cursorTime = last.getNextFireTime();
                cursorId = last.getId();
            }
        } catch (Exception e) {
            log.error("scan loop error", e);
        }
    }

    private void tryClaimAndEnqueue(Task task) {
        RLock lock = redisson.getLock(TaskConstants.CLAIM_LOCK_PREFIX + task.getId());
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 5, TimeUnit.SECONDS);
            if (!locked) {
                return;
            }
            int affected = taskMapper.casClaim(task.getId(), task.getVersion());
            if (affected == 1) {
                Task fresh = taskMapper.selectById(task.getId());
                long token = fresh != null ? fresh.getFencingToken() : -1;
                RBlockingQueue<Long> queue = redisson.getBlockingQueue(TaskConstants.QUEUE_NAME);
                queue.put(task.getId());
                log.info("claimed id={}, token={}, put queue", task.getId(), token);
            }
            // affected == 0：已被其他节点抢占，跳过
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("claim id={} failed", task.getId(), e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
