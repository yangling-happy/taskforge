package com.taskforge.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** POST /api/task 响应体 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTaskResponse {

    private Long id;
}
