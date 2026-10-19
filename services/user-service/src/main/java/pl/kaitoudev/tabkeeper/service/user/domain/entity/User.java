package pl.kaitoudev.tabkeeper.service.user.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    public enum Status {
        ACTIVE, DISABLED
    }

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status =  Status.ACTIVE;

    @Column(name = "created_ts", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdTimestamp = Instant.now();

    @Column(name = "updated_ts", nullable = false)
    @Builder.Default
    private Instant updatedTimestamp = Instant.now();

    @OneToOne(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private UserCredential credentials;

    public void setCredentials(UserCredential credentials) {
        this.credentials = Objects.requireNonNull(credentials);
        this.credentials.setUser(this);
    }
}