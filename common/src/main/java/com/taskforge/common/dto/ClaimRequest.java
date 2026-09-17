package com.taskforge.common.dto;

import lombok.Data;

/** Worker 领取任务请求体：POST /api/task/{id}/claim */
@Data
public class ClaimRequest {

    private String workerId;
}
