package pl.kaitoudev.tabkeeper.service.group.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.GroupInvitation;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, UUID> {

  @Query(
      """
    SELECT (COUNT(g) > 0)
    FROM GroupInvitation g
    WHERE g.group.id = :groupId
      AND g.invitedUser = :invitedUserId
      AND g.status = pl.kaitoudev.tabkeeper.service.group.domain.enums.GroupInvitationStatus.PENDING
    """)
  boolean isUserAlreadyInvited(
      @Param("groupId") UUID groupId, @Param("invitedUserId") UUID invitedUserId);
}
