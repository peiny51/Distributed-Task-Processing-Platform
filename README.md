# Distributed Task Processing Platform

A backend task processing system built with Java and Spring Boot. The platform supports asynchronous task execution, Redis-based task queues, transactional consistency, idempotent task creation, retries, and worker failure recovery.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Redis
- Maven

## Architecture

Client
  ↓
Spring Boot REST API
  ↓
PostgreSQL
  ↓
Transactional Outbox
  ↓
Redis Queue
  ↓
Task Worker
  ↓
Task Processing / Result

PostgreSQL is used as the source of truth for task state, while Redis provides asynchronous task distribution.

## Features

- REST APIs for task creation and status queries
- Idempotent task creation using idempotency keys
- Asynchronous task processing with Redis
- Transactional Outbox pattern
- Optimistic and pessimistic locking
- Automatic retry and failure handling
- Recovery of stale processing tasks
- Task status history and result tracking
- CSV processing as an example background task

## Task Lifecycle

QUEUED → PROCESSING → COMPLETED

Failed tasks are retried automatically and transition to `FAILED` after reaching the retry limit.

## Running Locally

Requirements:

- Java 21
- PostgreSQL
- Redis

Start PostgreSQL and Redis, configure the database connection in `application.properties`, then run:

```bash
./mvnw spring-boot:run

The API runs on:

http://localhost:8080

Example

Create a task:

POST /api/tasks
Idempotency-Key: example-key
Content-Type: application/json
{
  "taskType": "DATA_CLEANING",
  "inputPath": "/tmp/input.csv"
}

Check task status:

GET /api/tasks/{id}

Task history:

GET /api/tasks/{id}/history

Task result:

GET /api/tasks/{id}/result
