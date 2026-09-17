# TaskForge

A distributed job scheduling platform MVP for code build and automated test workloads.

## Key Features

- **Distributed claiming**: MySQL CAS + Redisson lock prevents duplicate claims across nodes
- **Lease + Fencing Token**: crashed workers are auto-reclaimed; stale submissions are rejected with `409 Conflict`
- **Load balancing**: Redis blocking queue naturally distributes tasks across workers
- **Retry with exponential backoff**: `1s / 2s / 4s` up to `max_retry`

## Architecture

```
Scheduler (main process)
  ├── scan thread: cursor pagination -> CAS claim -> put to Redis queue
  └── HTTP API: submit / claim / heartbeat / progress / submit-result / SSE

Worker (independent process)
  └── take from Redis queue -> claim -> run shell -> report progress -> submit

MySQL: single source of truth for task state machine
Redis: ready-notification queue only (Redisson)
```

## Tech Stack

Spring Boot 3.2 (Java 17) / MyBatis-Plus / MySQL 8 / Flyway / Redisson 3.27 / Redis 7 / Gradle (Kotlin DSL) / docker-compose

## Modules

```
taskforge/
├── common/      # shared entity, DTO, enum, constants
├── scheduler/   # scheduler main process (port 8080)
└── worker/      # worker main process (independent deployment)
```

## Quick Start

```bash
# start all services (mysql, redis, scheduler, worker-a, worker-b)
docker compose up -d --build
```

## Usage

Submit a task:

```bash
curl -X POST http://localhost:8080/api/task \
  -H "Content-Type: application/json" \
  -d '{"type": "SHELL", "payload": "echo hello", "delaySeconds": 0}'
```

Watch task state in MySQL:

```sql
SELECT id, status, worker_id, fencing_token, retry_count FROM task;
```

## Task State Machine

```
PENDING -> CLAIMED -> RUNNING -> SUCCESS
    ^          |           |
    |          | lease expired         | timeout / non-zero exit
    +----------+-----------+----------> FAILED (retry >= max_retry)
```
