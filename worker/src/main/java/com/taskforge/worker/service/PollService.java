package com.taskforge.worker.service;

import com.taskforge.common.constant.TaskConstants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Worker 主循环占位：阻塞 take 队列，拿到任务后只打日志。
 * TODO: 接入完整执行链路 claim -> 心跳 -> ProcessBuilder -> submit
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PollService {

    private final RedissonClient redisson;

    @Value("${worker.id:worker-local}")
    private String workerId;

    private Thread pollThread;

    @PostConstruct
    public void start() {
        pollThread = new Thread(this::loop, "worker-poll-thread");
        pollThread.setDaemon(true);
        pollThread.start();
        log.info("PollService started, workerId={}", workerId);
    }

    @PreDestroy
    public void stop() {
        if (pollThread != null) {
            pollThread.interrupt();
        }
    }

    private void loop() {
        RBlockingQueue<Long> queue = redisson.getBlockingQueue(TaskConstants.QUEUE_NAME);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Long taskId = queue.take();
                log.info("took taskId={}, workerId={} (TODO: claim + execute)", taskId, workerId);
                // TODO(用户): 完整执行链路
                //  1. POST /api/task/{id}/claim 带 workerId -> 拿 fencingToken + payload
                //  2. 启动心跳线程（每 10s POST /heartbeat 续约）
                //  3. ProcessBuilder 跑 shell，逐行读 stdout/stderr，每 1s/100 行 POST /progress
                //  4. POST /api/task/{id}/submit 带 fencingToken，409 则记 stale submit rejected
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("poll loop error", e);
            }
        }
    }
}
