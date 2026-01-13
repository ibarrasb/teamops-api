# syntax=docker/dockerfile:1.7

########################
# 1) Build stage
########################
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Leverage Docker layer caching for deps
COPY pom.xml ./
COPY mvnw ./
COPY .mvn/ .mvn/

# Cache Maven repo between builds (works great with buildx/buildkit)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -DskipTests dependency:go-offline

# Copy source last (so code changes don't bust dependency cache)
COPY src/ src/

# Build and normalize output jar to a known path
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -DskipTests clean package \
 && JAR="$(ls -1 target/*.jar | grep -v 'original' | head -n 1)" \
 && cp "$JAR" /app/app.jar

########################
# 2) Runtime stage
########################
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as non-root (better security)
RUN useradd -m -u 10001 appuser
USER appuser

COPY --from=build /app/app.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

# exec = proper signal handling (important for docker stop / ECS later)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
