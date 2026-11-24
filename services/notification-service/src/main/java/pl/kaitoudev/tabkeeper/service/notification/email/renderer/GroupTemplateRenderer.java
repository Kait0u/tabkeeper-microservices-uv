package pl.kaitoudev.tabkeeper.service.notification.email.renderer;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.event.group.GroupInvitationAcceptedEvent;
import pl.kaitoudev.tabkeeper.event.group.GroupInvitationCreatedEvent;

/** Renders email templates for group-invitation events. */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupTemplateRenderer {
  private final GenericTemplateRenderer genericTemplateRenderer;

  /** Renders the invitation sent to the invited user. */
  public String renderGroupInvitationCreatedEmail(
      GroupInvitationCreatedEvent event, String invitedByDisplayName) {
    log.info("Rendering email for GroupInvitationCreatedEvent (EventID={})", event.getEventId());

    Map<String, Object> variables =
        Map.ofEntries(
            Map.entry("groupName", event.getGroupName()),
            Map.entry("invitedByDisplayName", invitedByDisplayName));

    return genericTemplateRenderer.render("mail/group-invitation-created", variables);
  }

  /** Renders the invitation confirmation sent to the inviter. */
  public String renderGroupInvitationCreatedForInviterEmail(
      GroupInvitationCreatedEvent event, String invitedUserDisplayName) {
    log.info(
        "Rendering inviter email for GroupInvitationCreatedEvent (EventID={})", event.getEventId());

    Map<String, Object> variables =
        Map.ofEntries(
            Map.entry("groupName", event.getGroupName()),
            Map.entry("invitedUserDisplayName", invitedUserDisplayName));

    return genericTemplateRenderer.render("mail/group-invitation-created-inviter", variables);
  }

  /** Renders the acceptance notification sent to the inviter. */
  public String renderGroupInvitationAcceptedEmail(
      GroupInvitationAcceptedEvent event, String invitedUserDisplayName) {
    log.info("Rendering email for GroupInvitationAcceptedEvent (EventID={})", event.getEventId());

    Map<String, Object> variables =
        Map.ofEntries(
            Map.entry("groupName", event.getGroupName()),
            Map.entry("invitedUserDisplayName", invitedUserDisplayName));

    return genericTemplateRenderer.render("mail/group-invitation-accepted", variables);
  }

  /** Renders the welcome notification sent to the new group member. */
  public String renderGroupInvitationAcceptedForMemberEmail(GroupInvitationAcceptedEvent event) {
    log.info(
        "Rendering member email for GroupInvitationAcceptedEvent (EventID={})", event.getEventId());

    Map<String, Object> variables = Map.ofEntries(Map.entry("groupName", event.getGroupName()));

    return genericTemplateRenderer.render("mail/group-invitation-accepted-member", variables);
  }
}
