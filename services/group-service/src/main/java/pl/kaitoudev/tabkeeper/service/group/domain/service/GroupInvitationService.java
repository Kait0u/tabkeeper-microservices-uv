package pl.kaitoudev.tabkeeper.service.group.domain.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.Group;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.GroupInvitation;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.AlreadyAMemberException;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.AlreadyInvitedException;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.NonexistentGroupException;
import pl.kaitoudev.tabkeeper.service.group.domain.repository.GroupInvitationRepository;
import pl.kaitoudev.tabkeeper.service.group.domain.repository.GroupRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupInvitationService {
  private final GroupInvitationRepository groupInvitationRepository;
  private final GroupRepository groupRepository;

  public GroupInvitation inviteUserToGroup(UUID groupId, UUID invitedByUserId, UUID invitedUserId) {
    log.info(
        "Inviting user {} to group {} on behalf of {}.", invitedUserId, groupId, invitedByUserId);

    if (groupInvitationRepository.isUserAlreadyInvited(groupId, invitedUserId)) {
      log.warn("User {} is already invited.", invitedUserId);
      throw AlreadyInvitedException.ofId(groupId, invitedUserId);
    }

    Optional<Group> optGroup = groupRepository.findById(groupId);

    if (optGroup.isEmpty()) {
      log.warn("Group {} does not exist.", groupId);
      throw NonexistentGroupException.ofId(groupId);
    }

    Group group = optGroup.get();

    if (groupRepository.userBelongsToGroup(groupId, invitedUserId)) {
      log.warn("User {} is already belongs to group {}.", invitedUserId, groupId);
      throw AlreadyAMemberException.ofId(groupId, invitedUserId);
    }

    GroupInvitation groupInvitation =
        GroupInvitation.builder()
            .group(group)
            .invitedUser(invitedUserId)
            .invitedBy(invitedByUserId)
            .build();

    GroupInvitation saved = groupInvitationRepository.save(groupInvitation);
    log.info("InvitationCreated: ID={}", saved.getId());

    return saved;
  }
}
