package com.teamops.api.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
  List<Task> findAllByOwnerEmailAndProjectIdOrderByCreatedAtDesc(String ownerEmail, UUID projectId);

  Optional<Task> findByIdAndOwnerEmailAndProjectId(UUID id, String ownerEmail, UUID projectId);
}
