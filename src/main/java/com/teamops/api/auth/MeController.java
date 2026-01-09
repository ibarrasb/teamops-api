package com.teamops.api.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api")
public class MeController {

  public record MeResponse(String email, Collection<? extends GrantedAuthority> authorities) {}

  @GetMapping("/me")
  public MeResponse me(@AuthenticationPrincipal UserDetails user) {
    return new MeResponse(user.getUsername(), user.getAuthorities());
  }
}
