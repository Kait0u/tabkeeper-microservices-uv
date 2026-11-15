package pl.kaitoudev.tabkeeper.gateway.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.kaitoudev.tabkeeper.gateway.client.GroupGrpcClient;
import pl.kaitoudev.tabkeeper.gateway.domain.group.GroupDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.InvitationCreatedDetails;
import pl.kaitoudev.tabkeeper.gateway.domain.group.MembershipDetails;
import pl.kaitoudev.tabkeeper.gateway.dto.group.CheckMembershipResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.group.CreateGroupRequest;
import pl.kaitoudev.tabkeeper.gateway.dto.group.CreateGroupResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.group.GetGroupDetailsResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.group.GroupMemberResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.group.GroupSummaryResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.group.InviteUserToGroupRequest;
import pl.kaitoudev.tabkeeper.gateway.dto.group.InviteUserToGroupResponse;

/** Exposes groups and their nested resources without authenticating request-supplied user IDs. */
@Tag(name = "Groups")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
  private final GroupGrpcClient groupGrpcClient;

  /** Creates a group and returns its details with a Location header. */
  @PostMapping
  public ResponseEntity<CreateGroupResponse> createGroup(
      @Valid @RequestBody CreateGroupRequest request) {
    GroupDetails group =
        groupGrpcClient.createGroup(
            request.ownerId().toString(), request.groupName(), request.currencyCode());

    CreateGroupResponse responseDto =
        CreateGroupResponse.builder()
            .groupId(group.groupId())
            .groupName(group.groupName())
            .currencyCode(group.currencyCode())
            .members(toMemberResponses(group))
            .createdAt(group.createdAt())
            .build();
    URI location = URI.create("/api/v1/groups/" + group.groupId());

    return ResponseEntity.created(location).body(responseDto);
  }

  /** Retrieves a group and its current members. */
  @GetMapping("/{group_id}")
  public ResponseEntity<GetGroupDetailsResponse> getGroupDetails(
      @PathVariable("group_id") UUID groupId) {
    GroupDetails group = groupGrpcClient.getGroup(groupId.toString());

    GetGroupDetailsResponse responseDto =
        GetGroupDetailsResponse.builder()
            .groupId(group.groupId())
            .groupName(group.groupName())
            .currencyCode(group.currencyCode())
            .members(toMemberResponses(group))
            .createdAt(group.createdAt())
            .build();

    return ResponseEntity.ok(responseDto);
  }

  /**
   * Lists groups for the required user_id filter, rather than exposing an unfiltered collection.
   */
  @GetMapping
  public ResponseEntity<List<GroupSummaryResponse>> getGroupsForUser(
      @RequestParam("user_id") UUID userId) {
    List<GroupSummaryResponse> responseDto =
        groupGrpcClient.getGroupsForUser(userId.toString()).stream()
            .map(
                group ->
                    GroupSummaryResponse.builder()
                        .groupId(group.groupId())
                        .groupName(group.groupName())
                        .isOwner(group.isOwner())
                        .createdAt(group.createdAt())
                        .build())
            .toList();

    return ResponseEntity.ok(responseDto);
  }

  /** Retrieves membership and ownership flags for a specific user and group. */
  @GetMapping("/{group_id}/members/{user_id}")
  public ResponseEntity<CheckMembershipResponse> checkMembership(
      @PathVariable("group_id") UUID groupId, @PathVariable("user_id") UUID userId) {
    MembershipDetails membership =
        groupGrpcClient.checkMembership(groupId.toString(), userId.toString());

    CheckMembershipResponse responseDto =
        CheckMembershipResponse.builder()
            .groupId(membership.groupId())
            .userId(membership.userId())
            .isMember(membership.isMember())
            .isOwner(membership.isOwner())
            .build();

    return ResponseEntity.ok(responseDto);
  }

  /** Creates an invitation and points to its retrievable REST resource. */
  @PostMapping("/{group_id}/invitations")
  public ResponseEntity<InviteUserToGroupResponse> inviteUserToGroup(
      @PathVariable("group_id") UUID groupId,
      @Valid @RequestBody InviteUserToGroupRequest request) {
    InvitationCreatedDetails invitation =
        groupGrpcClient.inviteUserToGroup(
            groupId.toString(),
            request.invitedByUserId().toString(),
            request.invitedUserId().toString());

    InviteUserToGroupResponse responseDto =
        InviteUserToGroupResponse.builder()
            .invitationId(invitation.invitationId())
            .groupId(invitation.groupId())
            .invitedByUserId(invitation.invitedByUserId())
            .invitedUserId(invitation.invitedUserId())
            .build();
    URI location = URI.create("/api/v1/invitations/" + invitation.invitationId());

    return ResponseEntity.created(location).body(responseDto);
  }

  /** Converts gateway membership models to REST response objects. */
  private static List<GroupMemberResponse> toMemberResponses(GroupDetails group) {
    return group.members().stream()
        .map(
            member ->
                GroupMemberResponse.builder()
                    .userId(member.userId())
                    .role(member.role())
                    .joinedAt(member.joinedAt())
                    .build())
        .toList();
  }
}
