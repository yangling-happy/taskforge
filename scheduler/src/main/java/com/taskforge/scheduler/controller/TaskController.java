package com.taskforge.scheduler.controller;

import com.taskforge.common.dto.SubmitTaskRequest;
import com.taskforge.common.dto.SubmitTaskResponse;
import com.taskforge.common.entity.Task;
import com.taskforge.common.enums.TaskStatus;
import com.taskforge.scheduler.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskMapper taskMapper;

    /** 提交任务：落库 PENDING + next_fire_time，等扫描线程领取 */
    @PostMapping
    public SubmitTaskResponse submit(@RequestBody SubmitTaskRequest req) {
        if (req.getPayload() == null || req.getPayload().isBlank()) {
            throw new IllegalArgumentException("payload is required");
        }
        Task task = new Task();
        task.setType(req.getType() == null || req.getType().isBlank() ? "SHELL" : req.getType());
        task.setPayload(req.getPayload());
        task.setStatus(TaskStatus.PENDING.name());
        long delay = req.getDelaySeconds() == null ? 0 : req.getDelaySeconds();
        task.setNextFireTime(LocalDateTime.now().plusSeconds(delay));
        task.setVersion(0L);
        task.setRetryCount(0);
        task.setMaxRetry(3);
        task.setFencingToken(0L);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.insert(task);
        log.info("task submitted id={}, payload={}", task.getId(), req.getPayload());
        return new SubmitTaskResponse(task.getId());
    }
}
