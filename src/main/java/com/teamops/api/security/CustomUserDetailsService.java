package com.teamops.api.security;

import com.teamops.api.user.User;
import com.teamops.api.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository users;

  public CustomUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User u = users.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    // Spring Security expects roles like ROLE_USER
    String role = u.getRole() == null ? "USER" : u.getRole().toUpperCase();
    return new org.springframework.security.core.userdetails.User(
        u.getEmail(),
        u.getPasswordHash(),
        List.of(new SimpleGrantedAuthority("ROLE_" + role))
    );
  }
}
