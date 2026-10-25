package pl.kaitoudev.tabkeeper.service.group.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "groups", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "created_by"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(name = "created_ts", nullable = false)
  @Builder.Default
  private Instant createdTimestamp = Instant.now();

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<GroupMembership> groupMemberships = new HashSet<>();

  @OneToMany(mappedBy = "group") // TODO: Figure out the cascade.
  @Builder.Default
  private Set<GroupInvitation> groupInvitations = new HashSet<>();

  public void addGroupMembership(GroupMembership groupMembership) {
    groupMemberships.add(groupMembership);
    groupMembership.setGroup(this);
  }

  public void removeGroupMembership(GroupMembership groupMembership) {
    groupMemberships.remove(groupMembership);
    groupMembership.setGroup(null);
  }

  public void addGroupInvitation(GroupInvitation groupInvitation) {
    groupInvitations.add(groupInvitation);
    groupInvitation.setGroup(this);
  }
}
