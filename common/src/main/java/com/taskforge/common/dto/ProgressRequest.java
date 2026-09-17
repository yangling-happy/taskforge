package com.taskforge.common.dto;

import lombok.Data;

/** Worker 上报进度请求体：POST /api/task/{id}/progress */
@Data
public class ProgressRequest {

    private String workerId;

    private String stdout;

    private String stderr;
}
