package com.teamops.api.task;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    UUID projectId,
    String title,
    String status,
    OffsetDateTime dueAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
  public static TaskResponse from(Task t) {
    return new TaskResponse(
        t.getId(),
        t.getProjectId(),
        t.getTitle(),
        t.getStatus(),
        t.getDueAt(),
        t.getCreatedAt(),
        t.getUpdatedAt()
    );
  }
}
