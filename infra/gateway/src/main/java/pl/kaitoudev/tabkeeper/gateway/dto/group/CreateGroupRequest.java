package pl.kaitoudev.tabkeeper.gateway.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Group creation input; the owner identifier is supplied without authentication. */
public record CreateGroupRequest(@NotNull UUID ownerId, @NotBlank String groupName) {}
