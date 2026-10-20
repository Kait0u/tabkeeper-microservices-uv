package pl.kaitoudev.tabkeeper.service.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_credentials")
public class UserCredential {

  public static UserCredential ofPasswordHash(String passwordHash) {
    UserCredential userCredential = new UserCredential();
    userCredential.setPasswordHash(passwordHash);

    return userCredential;
  }

  @Id
  @Column(name = "user_id")
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "password_hash", nullable = false, columnDefinition = "text")
  private String passwordHash;

  @Column(name = "created_ts", nullable = false, updatable = false)
  private Instant createdTimestamp = Instant.now();

  @Column(name = "updated_ts", nullable = false)
  private Instant updatedTs = Instant.now();
}
