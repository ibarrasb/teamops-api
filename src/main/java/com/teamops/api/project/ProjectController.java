package com.teamops.api.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

  private final ProjectRepository projects;

  public ProjectController(ProjectRepository projects) {
    this.projects = projects;
  }

  @GetMapping
  public ResponseEntity<List<ProjectResponse>> list(@AuthenticationPrincipal UserDetails user) {
    List<ProjectResponse> out = projects
        .findAllByOwnerEmailOrderByCreatedAtDesc(user.getUsername())
        .stream()
        .map(ProjectResponse::from)
        .toList();

    return ResponseEntity.ok(out);
  }

  @GetMapping("/{projectId}")
  public ResponseEntity<ProjectResponse> getOne(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID projectId
  ) {
    Project p = projects.findByIdAndOwnerEmail(projectId, user.getUsername())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    return ResponseEntity.ok(ProjectResponse.from(p));
  }

  @PostMapping
  public ResponseEntity<ProjectResponse> create(
      @AuthenticationPrincipal UserDetails user,
      @Valid @RequestBody CreateProjectRequest req
  ) {
    Project p = Project.builder()
        .name(req.getName().trim())
        .description(req.getDescription())
        .ownerEmail(user.getUsername())
        .build();

    Project saved = projects.save(p);
    return ResponseEntity.status(HttpStatus.CREATED).body(ProjectResponse.from(saved));
  }

  @PatchMapping("/{projectId}")
  public ResponseEntity<ProjectResponse> update(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID projectId,
      @RequestBody UpdateProjectRequest req
  ) {
    Project p = projects.findByIdAndOwnerEmail(projectId, user.getUsername())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    boolean changed = false;

    if (req.getName() != null) {
      String name = req.getName().trim();
      if (name.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name cannot be blank");
      if (name.length() < 2 || name.length() > 160) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must be 2-160 characters");
      }
      p.setName(name);
      changed = true;
    }

    if (req.getDescription() != null) {
      p.setDescription(req.getDescription());
      changed = true;
    }

    if (!changed) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No updatable fields provided");
    }

    Project saved = projects.save(p);
    return ResponseEntity.ok(ProjectResponse.from(saved));
  }

  @DeleteMapping("/{projectId}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID projectId
  ) {
    Project p = projects.findByIdAndOwnerEmail(projectId, user.getUsername())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    projects.delete(p);
    return ResponseEntity.noContent().build();
  }

  public static class CreateProjectRequest {
    @NotBlank
    @Size(min = 2, max = 160)
    private String name;

    @Size(max = 1000)
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
  }

  public static class UpdateProjectRequest {
    private String name;
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
  }
}
