package pl.kaitoudev.tabkeeper.gateway.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kaitoudev.tabkeeper.gateway.client.GroupGrpcClient;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationAcceptedDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationDetails;
import pl.kaitoudev.tabkeeper.gateway.dto.group.AcceptGroupInvitationResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.group.GetInvitationDetailsResponse;

/** Exposes invitation resources using the Group Service's current no-auth contract. */
@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class GroupInvitationController {
  private final GroupGrpcClient groupGrpcClient;

  /** Retrieves invitation state, representing an absent acceptance timestamp as null. */
  @GetMapping("/{invitation_id}")
  public ResponseEntity<GetInvitationDetailsResponse> getInvitationDetails(
      @PathVariable("invitation_id") UUID invitationId) {
    InvitationDetails invitation = groupGrpcClient.getInvitation(invitationId.toString());

    GetInvitationDetailsResponse responseDto =
        GetInvitationDetailsResponse.builder()
            .invitationId(invitation.invitationId())
            .groupId(invitation.groupId())
            .groupName(invitation.groupName())
            .invitedByUserId(invitation.invitedByUserId())
            .invitedUserId(invitation.invitedUserId())
            .status(invitation.status())
            .createdAt(invitation.createdAt())
            .acceptedAt(invitation.acceptedAt().orElse(null))
            .build();

    return ResponseEntity.ok(responseDto);
  }

  /** Accepts an invitation and returns its invitation and group identifiers. */
  @PostMapping("/{invitation_id}/accept")
  public ResponseEntity<AcceptGroupInvitationResponse> acceptGroupInvitation(
      @PathVariable("invitation_id") UUID invitationId) {
    InvitationAcceptedDetails invitation =
        groupGrpcClient.acceptGroupInvitation(invitationId.toString());

    AcceptGroupInvitationResponse responseDto =
        AcceptGroupInvitationResponse.builder()
            .invitationId(invitation.invitationId())
            .groupId(invitation.groupId())
            .build();

    return ResponseEntity.ok(responseDto);
  }
}
