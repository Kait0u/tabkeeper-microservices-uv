package pl.kaitoudev.tabkeeper.service.group.domain.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.Group;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.GroupMembership;
import pl.kaitoudev.tabkeeper.service.group.domain.enums.GroupMembershipRole;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.GroupNameAlreadyTakenException;
import pl.kaitoudev.tabkeeper.service.group.domain.repository.GroupRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;

  public Optional<Group> getGroupById(UUID groupId) {
    return groupRepository.findById(groupId);
  }

  @Transactional
  public Group createGroup(String groupName, UUID creatorId) {
    if (groupRepository.existsByNameAndRole(groupName, creatorId, GroupMembershipRole.OWNER)) {
      log.warn("User {} already owns a group named \"{}\"", creatorId, groupName);

      throw GroupNameAlreadyTakenException.ofName(groupName);
    }

    Group group = Group.builder().name(groupName).createdBy(creatorId).build();

    GroupMembership ownership = GroupMembership.createOwner(creatorId);
    group.addGroupMembership(ownership); // This also sets ownership.group

    Group saved = groupRepository.save(group);

    log.info(
        "GroupCreated: ID={}, Name={}, CreatedBy={}",
        saved.getId(),
        saved.getName(),
        saved.getCreatedBy());

    return saved;
  }

  public List<Group> getGroupsForUser(UUID userId) {
    return groupRepository.findAllForUser(userId);
  }

  public List<Group> getOwnedGroupsForUser(UUID userId) {
    return groupRepository.findAllForUserWithRole(userId, GroupMembershipRole.OWNER);
  }

  public boolean checkMembership(UUID groupId, UUID userId) {
    // Will also return false for nonexistent groups.
    return groupRepository.userBelongsToGroup(groupId, userId);
  }

  public boolean checkOwnership(UUID groupId, UUID userId) {
    // Will also return false for nonexistent groups.
    return groupRepository.userOwnsGroup(groupId, userId);
  }
}
