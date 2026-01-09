package com.teamops.api.auth;

import com.teamops.api.security.JwtService;
import com.teamops.api.user.User;
import com.teamops.api.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authManager;
  private final JwtService jwt;

  public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authManager, JwtService jwt) {
    this.users = users;
    this.encoder = encoder;
    this.authManager = authManager;
    this.jwt = jwt;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
    String email = req.getEmail().toLowerCase();

    if (users.existsByEmail(email)) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    User u = new User();
    u.setEmail(email);
    u.setDisplayName(req.getDisplayName());
    u.setPasswordHash(encoder.encode(req.getPassword()));
    u.setRole("USER");

    users.save(u);

    String token = jwt.issueToken(u.getEmail(), u.getRole());
    return ResponseEntity.status(HttpStatus.CREATED).body(new AuthDtos.AuthResponse(token));
  }

  @PostMapping("/login")
  public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
    String email = req.getEmail().toLowerCase();

    authManager.authenticate(
        new UsernamePasswordAuthenticationToken(email, req.getPassword())
    );

    User u = users.findByEmail(email).orElseThrow();
    String token = jwt.issueToken(u.getEmail(), u.getRole());
    return new AuthDtos.AuthResponse(token);
  }
}
