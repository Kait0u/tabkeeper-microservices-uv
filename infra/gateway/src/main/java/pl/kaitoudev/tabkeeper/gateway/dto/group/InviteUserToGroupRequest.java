package pl.kaitoudev.tabkeeper.gateway.dto.group;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Invitation input; the group identifier comes from the request path. */
public record InviteUserToGroupRequest(
    @NotNull UUID invitedByUserId, @NotNull UUID invitedUserId) {}
