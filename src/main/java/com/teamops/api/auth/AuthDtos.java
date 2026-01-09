package com.teamops.api.auth;

import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDtos {

  @Getter @Setter
  public static class RegisterRequest {
    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 8, max = 72)
    private String password;

    @NotBlank @Size(min = 2, max = 120)
    private String displayName;
  }

  @Getter @Setter
  public static class LoginRequest {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;
  }

  @AllArgsConstructor
  @Getter
  public static class AuthResponse {
    private String token;
  }
}
