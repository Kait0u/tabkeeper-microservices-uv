package pl.kaitoudev.tabkeeper.service.notification.listeners;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.event.group.GroupInvitationAcceptedEvent;
import pl.kaitoudev.tabkeeper.event.group.GroupInvitationCreatedEvent;
import pl.kaitoudev.tabkeeper.service.notification.client.model.UserContactDetails;
import pl.kaitoudev.tabkeeper.service.notification.email.NotificationTitles;
import pl.kaitoudev.tabkeeper.service.notification.email.renderer.GroupTemplateRenderer;
import pl.kaitoudev.tabkeeper.service.notification.service.NotificationContactResolver;
import pl.kaitoudev.tabkeeper.service.notification.service.NotificationService;

/** Handles group-invitation events and notifies both concerned parties. */
@Slf4j
@Component
public class GroupNotificationListener extends AbstractNotificationListener {
  private final GroupTemplateRenderer groupTemplateRenderer;
  private final NotificationContactResolver contactResolver;

  public GroupNotificationListener(
      NotificationService notificationService,
      GroupTemplateRenderer groupTemplateRenderer,
      NotificationContactResolver contactResolver) {
    super(notificationService);
    this.groupTemplateRenderer = groupTemplateRenderer;
    this.contactResolver = contactResolver;
  }

  /** Sends the invitation to the invited user and a confirmation to the inviter. */
  @RabbitListener(queues = "groupinvite.created")
  public void handleGroupInvitationCreated(GroupInvitationCreatedEvent event) {
    log.info("Received GroupInvitationCreated event {}", event.getEventId());

    Map<UUID, UserContactDetails> contacts =
        contactResolver.resolveUsers(List.of(event.getInvitedByUserId(), event.getInvitedUserId()));
    UserContactDetails inviter = contacts.get(event.getInvitedByUserId());
    UserContactDetails invitedUser = contacts.get(event.getInvitedUserId());

    String invitedUserHtml =
        groupTemplateRenderer.renderGroupInvitationCreatedEmail(event, inviter.displayName());
    log.info(
        "Sending invitation email to user {} - {}...", invitedUser.userId(), invitedUser.email());
    notificationService.sendNotification(
        invitedUser.email(), NotificationTitles.INVITATION_CREATED, invitedUserHtml);

    String inviterHtml =
        groupTemplateRenderer.renderGroupInvitationCreatedForInviterEmail(
            event, invitedUser.displayName());
    log.info(
        "Sending invitation confirmation to user {} - {}...", inviter.userId(), inviter.email());
    notificationService.sendNotification(
        inviter.email(), NotificationTitles.INVITATION_CREATED, inviterHtml);

    log.info("GroupInvitationCreated event {} handled.", event.getEventId());
  }

  /** Notifies both the inviter and the new member that an invitation was accepted. */
  @RabbitListener(queues = "groupinvite.accepted")
  public void handleGroupInvitationAccepted(GroupInvitationAcceptedEvent event) {
    log.info("Received GroupInvitationAccepted event {}", event.getEventId());

    Map<UUID, UserContactDetails> contacts =
        contactResolver.resolveUsers(List.of(event.getInvitedByUserId(), event.getInvitedUserId()));
    UserContactDetails inviter = contacts.get(event.getInvitedByUserId());
    UserContactDetails invitedUser = contacts.get(event.getInvitedUserId());

    String inviterHtml =
        groupTemplateRenderer.renderGroupInvitationAcceptedEmail(event, invitedUser.displayName());
    log.info("Sending acceptance email to inviter {} - {}...", inviter.userId(), inviter.email());
    notificationService.sendNotification(
        inviter.email(), NotificationTitles.INVITATION_ACCEPTED, inviterHtml);

    String memberHtml = groupTemplateRenderer.renderGroupInvitationAcceptedForMemberEmail(event);
    log.info(
        "Sending welcome email to new member {} - {}...",
        invitedUser.userId(),
        invitedUser.email());
    notificationService.sendNotification(
        invitedUser.email(), NotificationTitles.INVITATION_ACCEPTED, memberHtml);

    log.info("GroupInvitationAccepted event {} handled.", event.getEventId());
  }
}
