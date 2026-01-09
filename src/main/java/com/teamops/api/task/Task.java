package com.teamops.api.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teamops.api.project.Project;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tasks")
public class Task {

  @Id
  @GeneratedValue
  private UUID id;

  // Keep this LAZY, but don't serialize it (prevents "no session" errors)
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  // Expose projectId to the client
  @JsonProperty("projectId")
  public UUID getProjectId() {
    return project != null ? project.getId() : null;
  }

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String status; // TODO, IN_PROGRESS, DONE

  @Column(name = "due_at")
  private OffsetDateTime dueAt;

  @Column(name = "owner_email", nullable = false)
  private String ownerEmail;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
