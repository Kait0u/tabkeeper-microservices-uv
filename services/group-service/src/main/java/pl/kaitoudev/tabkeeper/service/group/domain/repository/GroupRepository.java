package pl.kaitoudev.tabkeeper.service.group.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.Group;
import pl.kaitoudev.tabkeeper.service.group.domain.enums.GroupMembershipRole;

public interface GroupRepository extends JpaRepository<Group, UUID> {
  @Query(
      """
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM GroupMembership m
        WHERE m.group.name = :name
          AND m.userId = :userId
          AND m.role = :role
        """)
  boolean existsByNameAndRole(String name, UUID userId, GroupMembershipRole role);

  @Query(
      """
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM GroupMembership m
        WHERE m.group.id = :groupId
          AND m.userId = :userId
        """)
  boolean userBelongsToGroup(UUID groupId, UUID userId);

  @Query(
      """
      SELECT DISTINCT g
      FROM Group g
      JOIN g.groupMemberships m
      WHERE m.userId = :userId
      """)
  List<Group> findAllForUser(UUID userId);

  @Query(
      """
      SELECT DISTINCT g
      FROM Group g
      JOIN g.groupMemberships m
      WHERE m.userId = :userId
        AND m.role = :role
      """)
  List<Group> findAllForUserWithRole(UUID userId, GroupMembershipRole role);// WITH MEMBERSHIPS

  @EntityGraph(attributePaths = "groupMemberships")
  Optional<Group> findWithMembershipsByCreatedByAndName(UUID createdBy, String name);

  // WITH INVITATIONS

  @EntityGraph(attributePaths = "groupInvitations")
  List<Group> findWithInvitationsByCreatedBy(UUID createdBy);

  @EntityGraph(attributePaths = "groupInvitations")
  List<Group> findWithInvitationsByName(String name);

  @EntityGraph(attributePaths = "groupInvitations")
  Optional<Group> findWithInvitationsByCreatedByAndName(UUID createdBy, String name);
}
