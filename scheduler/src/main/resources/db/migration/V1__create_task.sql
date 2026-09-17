-- 任务表：状态机唯一真源
CREATE TABLE task (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  type            VARCHAR(32)  NOT NULL,           -- MVP 只用 'SHELL'
  payload         TEXT         NOT NULL,           -- shell 命令
  status          VARCHAR(16)  NOT NULL,           -- PENDING/CLAIMED/RUNNING/SUCCESS/FAILED
  next_fire_time  DATETIME(3)  NOT NULL,
  version         BIGINT       NOT NULL DEFAULT 0, -- 乐观锁
  retry_count     INT          NOT NULL DEFAULT 0,
  max_retry       INT          NOT NULL DEFAULT 3,
  fencing_token   BIGINT       NOT NULL DEFAULT 0, -- 领取时 +1，提交时校验
  lease_expire_at DATETIME(3)  NULL,               -- Worker 失联超时回收依据
  worker_id       VARCHAR(64)  NULL,
  last_error      TEXT         NULL,
  create_time     DATETIME(3)  NOT NULL,
  update_time     DATETIME(3)  NOT NULL,
  KEY idx_pending (status, next_fire_time, id)     -- 游标分页联合索引
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
