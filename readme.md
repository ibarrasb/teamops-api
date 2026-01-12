# teamops-api

Spring Boot (3.5.x) + Java 25 backend for TeamOps.  
JWT auth, Projects + Tasks CRUD, Flyway migrations, Postgres + Redis (local via Docker Compose).

## Requirements
- Java 25
- Docker + Docker Compose

## Local dev (recommended)

### 1) Start Postgres + Redis
From the folder that has your `docker-compose.yml` / `compose.yaml`:

```bash
docker compose up -d
```

Check status:

```bash
docker compose ps
```

Stop later:

```bash
docker compose down
```

### 2) Run the API (fast dev loop)
```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

### Optional: “fresh build” (skip tests)
This compiles everything clean and produces a runnable jar in `target/`.

```bash
./mvnw -q -DskipTests clean package
```

Run the built jar:

```bash
SPRING_PROFILES_ACTIVE=local java -jar target/*.jar
```

## Health checks (Actuator)

```bash
curl -i http://localhost:8080/actuator/health
curl -i http://localhost:8080/actuator/info
```

## Auth

### Register
> `displayName` is required.

```bash
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!","displayName":"Test User"}' | jq
```

### Login
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}' | jq -r .token)

echo "TOKEN=$TOKEN"
```

## Projects

### Create a project
```bash
PROJECT_ID=$(curl -s -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"My First Project","description":"testing"}' | jq -r .id)

echo "PROJECT_ID=$PROJECT_ID"
```

### List projects
```bash
curl -s http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Get one project
```bash
curl -s http://localhost:8080/api/projects/$PROJECT_ID \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Patch a project
```bash
curl -s -X PATCH http://localhost:8080/api/projects/$PROJECT_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated project name"}' | jq
```

### Delete a project
```bash
curl -s -i -X DELETE http://localhost:8080/api/projects/$PROJECT_ID \
  -H "Authorization: Bearer $TOKEN"
```

## Tasks

### Create a task under a project
```bash
TASK_ID=$(curl -s -X POST "http://localhost:8080/api/projects/$PROJECT_ID/tasks" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"First task","status":"TODO","dueAt":"2026-01-15T18:00:00Z"}' | jq -r .id)

echo "TASK_ID=$TASK_ID"
```

### List tasks
```bash
curl -s "http://localhost:8080/api/projects/$PROJECT_ID/tasks" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Helper: pick the newest task id (avoids jq null if list is empty)
```bash
TASK_ID=$(curl -s "http://localhost:8080/api/projects/$PROJECT_ID/tasks" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.[0].id // empty')

echo "TASK_ID=$TASK_ID"
```

### Get one task
```bash
curl -s "http://localhost:8080/api/projects/$PROJECT_ID/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Patch a task
```bash
curl -s -X PATCH "http://localhost:8080/api/projects/$PROJECT_ID/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated title","status":"IN_PROGRESS","dueAt":"2026-01-20T18:00:00Z"}' | jq
```

### Delete a task
```bash
curl -s -i -X DELETE "http://localhost:8080/api/projects/$PROJECT_ID/tasks/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN"
```

## Useful one-liners

### Start infra + run API
```bash
docker compose up -d && SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

### Start infra + fresh build + run (jar)
```bash
docker compose up -d && ./mvnw -q -DskipTests clean package && SPRING_PROFILES_ACTIVE=local java -jar target/*.jar
```

## Production (AWS)

Use the `prod` profile + env vars.

### Required env vars (match `application-prod.yml`)
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USER`
- `DB_PASS`
- `JWT_SECRET`
- `PORT` (optional, defaults to 8080)
- `REDIS_HOST` (optional)
- `REDIS_PORT` (optional, defaults to 6379)

Flyway migrations run automatically on startup.
