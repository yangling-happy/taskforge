package com.taskforge.common.dto;

import lombok.Data;

/** Worker 提交最终结果请求体：POST /api/task/{id}/submit */
@Data
public class SubmitResultRequest {

    private String workerId;

    private long fencingToken;

    /** SUCCESS / FAILED */
    private String status;

    private String output;

    private String error;
}
