package pl.kaitoudev.tabkeeper.service.group.domain.service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.kaitoudev.tabkeeper.event.group.GroupInvitationAcceptedEvent;
import pl.kaitoudev.tabkeeper.event.group.GroupInvitationCreatedEvent;
import pl.kaitoudev.tabkeeper.publisher.NotificationEventPublisher;
import pl.kaitoudev.tabkeeper.queues.QueueNames;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.Group;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.GroupInvitation;
import pl.kaitoudev.tabkeeper.service.group.domain.enums.GroupInvitationStatus;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.AlreadyAMemberException;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.AlreadyInvitedException;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.NonexistentGroupException;
import pl.kaitoudev.tabkeeper.service.group.domain.exception.NonexistentInvitationException;
import pl.kaitoudev.tabkeeper.service.group.domain.repository.GroupInvitationRepository;
import pl.kaitoudev.tabkeeper.service.group.domain.repository.GroupRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupInvitationService {

  private final GroupMembershipService groupMembershipService;
  private final GroupInvitationRepository groupInvitationRepository;
  private final GroupRepository groupRepository;
  private final NotificationEventPublisher notificationEventPublisher;

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

    publishInvitationSentNotification(groupInvitation);

    return saved;
  }

  public GroupInvitation getGroupInvitation(UUID invitationId) {
    log.info("Getting group invitation by ID={}", invitationId);
    Optional<GroupInvitation> optInvitation = groupInvitationRepository.findById(invitationId);

    if (optInvitation.isEmpty()) {
      log.warn("Invitation not found for ID={}", invitationId);
      throw NonexistentInvitationException.ofId(invitationId);
    }

    return optInvitation.get();
  }

  public GroupInvitation getGroupInvitationWithGroup(UUID invitationId) {
    log.info("Getting group invitation by ID={} (with group)", invitationId);
    Optional<GroupInvitation> optInvitation =
        groupInvitationRepository.findWithGroupById(invitationId);

    if (optInvitation.isEmpty()) {
      log.warn("Invitation not found for ID={}", invitationId);
      throw NonexistentInvitationException.ofId(invitationId);
    }

    return optInvitation.get();
  }

  @Transactional
  public GroupInvitation acceptInvitation(UUID invitationId) {
    log.info("Accepting invitation by ID={}", invitationId);

    GroupInvitation groupInvitation = getGroupInvitationWithGroup(invitationId);

    groupInvitation.setStatus(GroupInvitationStatus.ACCEPTED);
    groupInvitation.setRespondedTimestamp(Instant.now());

    GroupInvitation saved = groupInvitationRepository.save(groupInvitation);

    groupMembershipService.addMemberToGroup(
        groupInvitation.getGroup().getId(), groupInvitation.getInvitedUser());
    log.info("InvitationAccepted: ID={}", saved.getId());

    publishInvitationAcceptedNotification(groupInvitation);

    return saved;
  }

  // -----------------------------------------------------------------------------------------------
  // NOTIFICATION LOGIC
  // -----------------------------------------------------------------------------------------------

  private void publishInvitationSentNotification(GroupInvitation groupInvitation) {
    Instant now = Instant.now();

    GroupInvitationCreatedEvent event = GroupInvitationCreatedEvent.builder()
        .eventId(UUID.randomUUID())
        .groupId(groupInvitation.getGroup().getId())
        .invitationId(groupInvitation.getId())
        .invitedUserId(groupInvitation.getInvitedUser())
        .invitedByUserId(groupInvitation.getInvitedBy())
        .groupName(groupInvitation.getGroup().getName())
        .occurredAt(now)
        .createdAt(now)
        .build();

    notificationEventPublisher.publishAfterCommit(event, QueueNames.GROUPINVITE_CREATED);
    log.info("{}@ID={} created for publishing: {}", event.getEventType(), event.getEventId(), event);
  }

  private void publishInvitationAcceptedNotification(GroupInvitation groupInvitation) {
    Instant now = Instant.now();

    GroupInvitationAcceptedEvent event = GroupInvitationAcceptedEvent.builder()
        .eventId(UUID.randomUUID())
        .groupId(groupInvitation.getGroup().getId())
        .invitationId(groupInvitation.getId())
        .groupName(groupInvitation.getGroup().getName())
        .invitedUserId(groupInvitation.getInvitedUser())
        .invitedByUserId(groupInvitation.getInvitedBy())
        .acceptedAt(now)
        .occurredAt(now)
        .build();

    notificationEventPublisher.publishAfterCommit(event, QueueNames.GROUPINVITE_ACCEPTED);
    log.info("{}@ID={} created for publishing: {}", event.getEventType(), event.getEventId(), event);
  }
}
