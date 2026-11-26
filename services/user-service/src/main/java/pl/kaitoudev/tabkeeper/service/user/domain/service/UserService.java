package pl.kaitoudev.tabkeeper.service.user.domain.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kaitoudev.tabkeeper.event.user.UserCreatedEvent;
import pl.kaitoudev.tabkeeper.publisher.NotificationEventPublisher;
import pl.kaitoudev.tabkeeper.queues.QueueNames;
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
  private final NotificationEventPublisher notificationEventPublisher;

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

    publishUserCreatedNotification(user);

    return user;
  }

  private boolean checkIfEmailTaken(String email) {
    return userRepository.existsByEmail(email);
  }

  // -----------------------------------------------------------------------------------------------
  // NOTIFICATION LOGIC
  // -----------------------------------------------------------------------------------------------

  private void publishUserCreatedNotification(User createdUser) {
    Instant now = Instant.now();

    UserCreatedEvent event = UserCreatedEvent.builder()
        .eventId(UUID.randomUUID())
        .occurredAt(now)
        .userId(createdUser.getId())
        .displayName(createdUser.getDisplayName())
        .email(createdUser.getEmail())
        .createdAt(now)
        .build();

    notificationEventPublisher.publishAfterCommit(event, QueueNames.USER_CREATED);
    log.info("{}@ID={} created for publishing: {}", event.getEventType(), event.getEventId(), event);
  }
}
