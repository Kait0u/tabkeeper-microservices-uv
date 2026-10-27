package pl.kaitoudev.tabkeeper.service.group.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import pl.kaitoudev.tabkeeper.service.group.domain.enums.GroupInvitationStatus;

@Entity
@Table(
    name = "group_invitations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "invited_user"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupInvitation {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "invited_user", nullable = false)
  private UUID invitedUser;

  @Column(name = "invited_by", nullable = false)
  private UUID invitedBy;

  @Column(name = "status", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private GroupInvitationStatus status = GroupInvitationStatus.PENDING;

  @Column(name = "responded_ts")
  private Instant respondedTimestamp;

  @CreationTimestamp
  @Column(name = "created_ts", nullable = false)
  private Instant createdTimestamp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;
}
