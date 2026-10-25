package pl.kaitoudev.tabkeeper.service.group.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kaitoudev.tabkeeper.service.group.domain.enums.GroupMembershipRole;

@Entity
@Table(name = "group_memberships")
@IdClass(GroupMembershipId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembership {

  @Id
  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @Column(name = "role", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private GroupMembershipRole role;

  @Column(name = "joined_ts", nullable = false)
  @Builder.Default
  private Instant joinedTimestamp = Instant.now();

  public static GroupMembership createOwner(UUID creatorId) {
    return builder().userId(creatorId).role(GroupMembershipRole.OWNER).build();
  }

  public static GroupMembership createOwner(Group group, UUID creatorId) {
    GroupMembership membership = createOwner(creatorId);
    membership.setGroup(group);
    return membership;
  }

  public static GroupMembership createMember(UUID userId) {
    return builder().userId(userId).role(GroupMembershipRole.MEMBER).build();
  }

  public static GroupMembership createMember(Group group, UUID userId) {
    GroupMembership membership = createMember(userId);
    membership.setGroup(group);
    return membership;
  }
}
