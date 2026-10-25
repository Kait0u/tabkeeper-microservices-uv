package pl.kaitoudev.tabkeeper.service.group.domain.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.Group;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.GroupMembership;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.NonexistentGroupException;
import pl.kaitoudev.tabkeeper.service.group.domain.repository.GroupInvitationRepository;
import pl.kaitoudev.tabkeeper.service.group.domain.repository.GroupMembershipRepository;
import pl.kaitoudev.tabkeeper.service.group.domain.repository.GroupRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMembershipService {
  private final GroupService groupService;
  private final GroupMembershipRepository groupMembershipRepository;
  private final GroupRepository groupRepository;
  private final GroupInvitationRepository groupInvitationRepository;

  public GroupMembership addMemberToGroup(UUID groupId, UUID userId) {
    log.info("Adding member to group {} for user {}", groupId, userId);

    Optional<Group> optGroup = groupService.getGroupById(groupId);

    if (optGroup.isEmpty()) {
      log.warn("Group {} does not exist.", groupId);
      throw NonexistentGroupException.ofId(groupId);
    }

    Group group = optGroup.get();
    GroupMembership groupMembership = GroupMembership.createMember(group, userId);
    GroupMembership saved = groupMembershipRepository.save(groupMembership);

    log.info("GroupMemberAdded: GroupID={}, MemberID={}", groupId, userId);

    return saved;
  }
}
