package com.taskforge.scheduler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskforge.common.entity.Task;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 游标分页扫描到期 PENDING 任务，利用 idx_pending 联合索引。
     * (next_fire_time, id) 元组比较保证不重不漏。
     */
    @Select("""
            SELECT * FROM task
             WHERE status = 'PENDING'
               AND next_fire_time <= NOW(3)
               AND (next_fire_time, id) > (#{cursorTime}, #{cursorId})
             ORDER BY next_fire_time, id
             LIMIT 100
            """)
    List<Task> scanPending(@Param("cursorTime") LocalDateTime cursorTime,
                           @Param("cursorId") long cursorId);

    /**
     * CAS 领取：PENDING -> CLAIMED，fencing_token 单调 +1，lease 30s。
     * 返回 0 表示已被其他节点抢占或版本冲突。
     */
    @Update("""
            UPDATE task SET
                status = 'CLAIMED',
                fencing_token = fencing_token + 1,
                lease_expire_at = DATE_ADD(NOW(3), INTERVAL 30 SECOND),
                worker_id = NULL,
                version = version + 1
             WHERE id = #{id} AND status = 'PENDING' AND version = #{version}
            """)
    int casClaim(@Param("id") long id, @Param("version") long version);
}
