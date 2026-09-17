package com.taskforge.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** MVP 只用 'SHELL' */
    private String type;

    /** shell 命令 */
    private String payload;

    /** PENDING/CLAIMED/RUNNING/SUCCESS/FAILED */
    private String status;

    private LocalDateTime nextFireTime;

    /** 乐观锁 */
    private Long version;

    private Integer retryCount;

    private Integer maxRetry;

    /** 领取时 +1，提交时校验，防过期提交 */
    private Long fencingToken;

    /** Worker 失联超时回收依据 */
    private LocalDateTime leaseExpireAt;

    private String workerId;

    private String lastError;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
