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
import org.springframework.web.server.ResponseStatusException;

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
  public ResponseEntity<List<TaskResponse>> list(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID projectId
  ) {
    requireOwnedProject(projectId, user.getUsername());

    List<TaskResponse> out = tasks
        .findAllByOwnerEmailAndProjectIdOrderByCreatedAtDesc(user.getUsername(), projectId)
        .stream()
        .map(TaskResponse::from)
        .toList();

    return ResponseEntity.ok(out);
  }

  @GetMapping("/{taskId}")
  public ResponseEntity<TaskResponse> getOne(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId
  ) {
    requireOwnedProject(projectId, user.getUsername());

    Task task = tasks.findByIdAndOwnerEmailAndProjectId(taskId, user.getUsername(), projectId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    return ResponseEntity.ok(TaskResponse.from(task));
  }

  @PostMapping
  public ResponseEntity<TaskResponse> create(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID projectId,
      @Valid @RequestBody CreateTaskRequest req
  ) {
    requireOwnedProject(projectId, user.getUsername());

    String status = TaskStatus.defaultIfBlank(req.getStatus());

    Task t = Task.builder()
        .projectId(projectId)
        .title(req.getTitle().trim())
        .status(status)
        .dueAt(req.getDueAt())
        .ownerEmail(user.getUsername())
        .build();

    Task saved = tasks.save(t);
    return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(saved));
  }

  @PatchMapping("/{taskId}")
  public ResponseEntity<TaskResponse> update(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @RequestBody UpdateTaskRequest req
  ) {
    requireOwnedProject(projectId, user.getUsername());

    Task task = tasks.findByIdAndOwnerEmailAndProjectId(taskId, user.getUsername(), projectId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    boolean changed = false;

    if (req.getTitle() != null) {
      String title = req.getTitle().trim();
      if (title.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title cannot be blank");
      if (title.length() < 2 || title.length() > 200) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must be 2-200 characters");
      }
      task.setTitle(title);
      changed = true;
    }

    if (req.getStatus() != null) {
      try {
        task.setStatus(TaskStatus.normalizeOrThrow(req.getStatus()));
      } catch (IllegalArgumentException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
      }
      changed = true;
    }

    if (req.getDueAt() != null) {
      task.setDueAt(req.getDueAt());
      changed = true;
    }

    if (!changed) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No updatable fields provided");
    }

    Task saved = tasks.save(task);
    return ResponseEntity.ok(TaskResponse.from(saved));
  }

  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID projectId,
      @PathVariable UUID taskId
  ) {
    requireOwnedProject(projectId, user.getUsername());

    Task task = tasks.findByIdAndOwnerEmailAndProjectId(taskId, user.getUsername(), projectId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    tasks.delete(task);
    return ResponseEntity.noContent().build();
  }

  private Project requireOwnedProject(UUID projectId, String ownerEmail) {
    Optional<Project> project = projects.findByIdAndOwnerEmail(projectId, ownerEmail);
    if (project.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    return project.get();
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

  public static class UpdateTaskRequest {
    private String title;
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
