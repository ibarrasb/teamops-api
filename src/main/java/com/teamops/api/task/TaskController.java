package com.teamops.api.task;

import com.teamops.api.project.Project;
import com.teamops.api.project.ProjectRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

  private final TaskRepository tasks;
  private final ProjectRepository projects;

  public TaskController(TaskRepository tasks, ProjectRepository projects) {
    this.tasks = tasks;
    this.projects = projects;
  }

  @GetMapping
  public ResponseEntity<List<Task>> list(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID projectId
  ) {
    Optional<Project> project = projects.findByIdAndOwnerEmail(projectId, user.getUsername());
    if (project.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    return ResponseEntity.ok(
        tasks.findAllByOwnerEmailAndProject_IdOrderByCreatedAtDesc(user.getUsername(), projectId)
    );
  }

  @PostMapping
  public ResponseEntity<Task> create(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID projectId,
      @Valid @RequestBody CreateTaskRequest req
  ) {
    Optional<Project> project = projects.findByIdAndOwnerEmail(projectId, user.getUsername());
    if (project.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    Task t = Task.builder()
        .project(project.get())
        .title(req.getTitle())
        .status(req.getStatus() == null ? "TODO" : req.getStatus())
        .dueAt(req.getDueAt())
        .ownerEmail(user.getUsername())
        .build();

    Task saved = tasks.save(t);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  public static class CreateTaskRequest {
    @NotBlank
    @Size(min = 2, max = 200)
    private String title;

    @Size(max = 30)
    private String status;

    private OffsetDateTime dueAt;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getDueAt() { return dueAt; }
    public void setDueAt(OffsetDateTime dueAt) { this.dueAt = dueAt; }
  }
}
