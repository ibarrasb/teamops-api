package com.teamops.api.project;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(
    UUID id,
    String name,
    String description,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
  public static ProjectResponse from(Project p) {
    return new ProjectResponse(
        p.getId(),
        p.getName(),
        p.getDescription(),
        p.getCreatedAt(),
        p.getUpdatedAt()
    );
  }
}
