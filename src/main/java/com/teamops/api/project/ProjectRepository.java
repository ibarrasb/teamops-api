package com.teamops.api.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
  List<Project> findAllByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);
  Optional<Project> findByIdAndOwnerEmail(UUID id, String ownerEmail);
}
