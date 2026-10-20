package pl.kaitoudev.tabkeeper.service.user.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SecurityService {
  private final PasswordEncoder passwordEncoder;

  public String encodePassword(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }
}
