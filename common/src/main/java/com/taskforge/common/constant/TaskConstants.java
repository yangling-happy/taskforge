package com.taskforge.common.constant;

public final class TaskConstants {

    /** Redis 就绪通知队列 key（Scheduler put / Worker take） */
    public static final String QUEUE_NAME = "task_queue";

    /** Scheduler 领取分布式锁前缀：claim:{taskId} */
    public static final String CLAIM_LOCK_PREFIX = "claim:";

    /** Scheduler 回收分布式锁前缀：reclaim:{taskId} */
    public static final String RECLAIM_LOCK_PREFIX = "reclaim:";

    /** 扫描游标分页批量大小 */
    public static final int SCAN_BATCH_SIZE = 100;

    /** lease 租约时长（秒） */
    public static final long LEASE_SECONDS = 30;

    private TaskConstants() {
    }
}
