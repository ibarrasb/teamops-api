package com.teamops.api.task;

import java.util.Set;

public final class TaskStatus {
  private TaskStatus() {}

  public static final String TODO = "TODO";
  public static final String IN_PROGRESS = "IN_PROGRESS";
  public static final String DONE = "DONE";

  private static final Set<String> ALLOWED = Set.of(TODO, IN_PROGRESS, DONE);

  public static String defaultIfBlank(String raw) {
    if (raw == null) return TODO;
    String s = raw.trim();
    return s.isEmpty() ? TODO : normalizeOrThrow(s);
  }

  public static String normalizeOrThrow(String raw) {
    if (raw == null) throw new IllegalArgumentException("status is required");
    String s = raw.trim().toUpperCase();

    // allow a couple common variants
    if (s.equals("INPROGRESS") || s.equals("IN-PROGRESS") || s.equals("IN PROGRESS")) {
      s = IN_PROGRESS;
    }

    if (!ALLOWED.contains(s)) {
      throw new IllegalArgumentException("Invalid status. Use TODO, IN_PROGRESS, or DONE.");
    }
    return s;
  }
}
