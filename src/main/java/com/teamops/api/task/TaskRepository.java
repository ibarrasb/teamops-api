package com.teamops.api.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
  List<Task> findAllByOwnerEmailAndProject_IdOrderByCreatedAtDesc(String ownerEmail, UUID projectId);
}
