package pl.kaitoudev.tabkeeper.service.user.domain.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kaitoudev.tabkeeper.service.user.domain.entity.User;
import pl.kaitoudev.tabkeeper.service.user.domain.entity.UserCredential;
import pl.kaitoudev.tabkeeper.service.user.domain.exception.EmailAlreadyTakenException;
import pl.kaitoudev.tabkeeper.service.user.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final AuthService authService;

  public Optional<User> getUserById(UUID id) {
    return userRepository.findById(id);
  }

  public Optional<User> getUserById(String id) {
    return userRepository.findById(UUID.fromString(id));
  }

  public User createUser(String displayName, String email, String plainPassword) {
    if (checkIfEmailTaken(email)) {
      log.error("Email {} is already taken", email);
      throw new EmailAlreadyTakenException(email);
    }

    User user = User.builder().displayName(displayName).email(email).build();

    UserCredential userCreds = authService.createCredential(plainPassword);
    user.setCredentials(userCreds);

    user = userRepository.save(user);
    log.info("UserCreated: ID={}, DisplayName={}, Email={}", user.getId(), displayName, email);

    return user;
  }

  private boolean checkIfEmailTaken(String email) {
    return userRepository.existsByEmail(email);
  }
}
