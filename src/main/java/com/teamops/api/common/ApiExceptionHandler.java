package com.teamops.api.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionHandler {

  // null-analysis is overly strict here; URI.create(...) returns non-null for valid inputs.
  @SuppressWarnings("null")
  private static final URI ABOUT_BLANK = Objects.requireNonNull(URI.create("about:blank"));

  private static void stamp(ProblemDetail pd, HttpServletRequest req) {
    pd.setType(ABOUT_BLANK);
    pd.setProperty("timestamp", OffsetDateTime.now().toString());
    pd.setProperty("path", req.getRequestURI());
  }

  // 400 - @Valid on request bodies
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, List<String>> errors = new LinkedHashMap<>();

    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      String msg = (fe.getDefaultMessage() != null) ? fe.getDefaultMessage() : "invalid";
      errors.computeIfAbsent(fe.getField(), k -> new ArrayList<>()).add(msg);
    }

    ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(400));
    pd.setTitle("Validation failed");
    pd.setDetail("One or more fields are invalid.");
    stamp(pd, req);
    pd.setProperty("errors", errors);

    return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(pd);
  }

  // 400 - validation for params if you use @Validated
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
    Map<String, List<String>> errors = new LinkedHashMap<>();

    for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
      String key = (v.getPropertyPath() != null) ? v.getPropertyPath().toString() : "param";
      String msg = (v.getMessage() != null) ? v.getMessage() : "invalid";
      errors.computeIfAbsent(key, k -> new ArrayList<>()).add(msg);
    }

    ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(400));
    pd.setTitle("Validation failed");
    pd.setDetail("One or more parameters are invalid.");
    stamp(pd, req);
    pd.setProperty("errors", errors);

    return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(pd);
  }

  // 400 - UUID parse errors, etc (e.g. /tasks/not-a-uuid)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    String param = (ex.getName() != null) ? ex.getName() : "parameter";

    Class<?> requiredType = ex.getRequiredType(); // may be null
    String expected = (requiredType != null) ? requiredType.getSimpleName() : "value";

    ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(400));
    pd.setTitle("Invalid parameter");
    pd.setDetail("Invalid " + expected + " for '" + param + "'.");
    stamp(pd, req);

    return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(pd);
  }

  // 401 - login failures (bad credentials, user not found, etc.)
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(401));
    pd.setTitle("Unauthorized");
    pd.setDetail("Invalid email or password.");
    stamp(pd, req);
    return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(pd);
  }

  // 403 - Spring Security
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(403));
    pd.setTitle("Forbidden");
    pd.setDetail("You do not have permission to perform this action.");
    stamp(pd, req);
    return ResponseEntity.status(HttpStatusCode.valueOf(403)).body(pd);
  }

  // 405 - wrong HTTP method (e.g. GET /auth/login)
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ProblemDetail> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
    Set<org.springframework.http.HttpMethod> supported = ex.getSupportedHttpMethods(); // may be null

    String allowed = "";
    if (supported != null && !supported.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (org.springframework.http.HttpMethod m : supported) {
        if (sb.length() > 0) sb.append(", ");
        sb.append(m.name());
      }
      allowed = sb.toString();
    }

    ProblemDetail pd = ProblemDetail.forStatus(HttpStatusCode.valueOf(405));
    pd.setTitle("Method Not Allowed");
    pd.setDetail(allowed.isBlank() ? "HTTP method not allowed for this endpoint." : "Use one of: " + allowed);
    stamp(pd, req);

    return ResponseEntity.status(HttpStatusCode.valueOf(405)).body(pd);
  }

  // 404/400/etc - what you throw via ResponseStatusException
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
    HttpStatusCode statusCode = ex.getStatusCode();

    ProblemDetail pd = ProblemDetail.forStatus(statusCode);
    HttpStatus hs = HttpStatus.resolve(statusCode.value());
    String title = (hs != null) ? hs.getReasonPhrase() : "Error";

    pd.setTitle(title);
    pd.setDetail(ex.getReason() != null ? ex.getReason() : title);
    stamp(pd, req);

    return ResponseEntity.status(statusCode).body(pd);
  }

  // Framework exceptions that already expose a ProblemDetail body
  @ExceptionHandler(ErrorResponseException.class)
  public ResponseEntity<ProblemDetail> handleErrorResponseException(ErrorResponseException ex, HttpServletRequest req) {
    ProblemDetail pd = ex.getBody();
    if (pd == null) {
      pd = ProblemDetail.forStatus(ex.getStatusCode());
      HttpStatus hs = HttpStatus.resolve(ex.getStatusCode().value());
      pd.setTitle(hs != null ? hs.getReasonPhrase() : "Error");
      pd.setDetail(pd.getTitle());
    }

    stamp(pd, req);
    return ResponseEntity.status(ex.getStatusCode()).body(pd);
  }

  // 500 - catch-all (no stack trace in response)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest req) {
    HttpStatusCode statusCode = HttpStatusCode.valueOf(500);

    ProblemDetail pd = ProblemDetail.forStatus(statusCode);
    pd.setTitle("Internal Server Error");
    pd.setDetail("Something went wrong.");
    stamp(pd, req);

    return ResponseEntity.status(statusCode).body(pd);
  }
}
