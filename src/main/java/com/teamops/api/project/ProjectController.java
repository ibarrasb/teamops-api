package com.teamops.api.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

  private final ProjectRepository projects;

  public ProjectController(ProjectRepository projects) {
    this.projects = projects;
  }

  private static boolean isAdmin(UserDetails user) {
    for (GrantedAuthority a : user.getAuthorities()) {
      if ("ROLE_ADMIN".equals(a.getAuthority())) return true;
    }
    return false;
  }

  @GetMapping
  public List<Project> list(@AuthenticationPrincipal UserDetails user) {
    // keep it simple: users see only their projects (admins can still see only theirs for now)
    return projects.findAllByOwnerEmailOrderByCreatedAtDesc(user.getUsername());
  }

  @PostMapping
  public ResponseEntity<Project> create(
      @AuthenticationPrincipal UserDetails user,
      @Valid @RequestBody CreateProjectRequest req
  ) {
    Project p = Project.builder()
        .name(req.getName())
        .description(req.getDescription())
        .ownerEmail(user.getUsername())
        .build();

    Project saved = projects.save(p);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Project> getById(
      @AuthenticationPrincipal UserDetails user,
      @PathVariable UUID id
  ) {
    return projects.findByIdAndOwnerEmail(id, user.getUsername())
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  // DELETE will be ADMIN-only via SecurityConfig change below
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    if (!projects.existsById(id)) return ResponseEntity.notFound().build();
    projects.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  public static class CreateProjectRequest {
    @NotBlank @Size(min = 2, max = 120)
    private String name;

    @Size(max = 500)
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
  }
}
