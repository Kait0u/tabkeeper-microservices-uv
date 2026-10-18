package pl.kaitoudev.tabkeeper.service.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;


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
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String passwordHash;

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime createdTimestamp = LocalDateTime.now();

    @Column(name = "created_ts", nullable = false)
    private LocalDateTime updatedTs = LocalDateTime.now();
}
