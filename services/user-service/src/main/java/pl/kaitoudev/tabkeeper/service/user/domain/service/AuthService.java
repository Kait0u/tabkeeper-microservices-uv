package pl.kaitoudev.tabkeeper.service.user.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kaitoudev.tabkeeper.service.user.domain.entity.UserCredential;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private final PasswordEncoder passwordEncoder;

  public UserCredential createCredential(String password) {
    return UserCredential.ofPasswordHash(passwordEncoder.encode(password));
  }
}
