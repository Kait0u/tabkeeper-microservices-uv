package pl.kaitoudev.tabkeeper.service.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Builder
@Entity
@Table(name = "users")
public class User {
    public enum Status {
        ACTIVE, INACTIVE
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
    private Status status;

    @Column(name = "created_ts", nullable = false)
    @Builder.Default
    private LocalDateTime createdTimestamp = LocalDateTime.now();

    @Column(name = "updated_ts", nullable = false)
    @Builder.Default
    private LocalDateTime updatedTimestamp = LocalDateTime.now();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credentials_id", nullable = false, unique = true)
    private UserCredential credentials;
}