package com.taskforge.common.dto;

import lombok.Data;

/** POST /api/task 请求体 */
@Data
public class SubmitTaskRequest {

    /** MVP 只支持 SHELL，空则默认 SHELL */
    private String type;

    /** shell 命令 */
    private String payload;

    /** 延迟执行秒数，0 表示立即 */
    private Long delaySeconds;
}
