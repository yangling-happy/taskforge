package com.taskforge.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Worker 领取任务响应体 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    private Long taskId;

    private long fencingToken;

    private String payload;
}
