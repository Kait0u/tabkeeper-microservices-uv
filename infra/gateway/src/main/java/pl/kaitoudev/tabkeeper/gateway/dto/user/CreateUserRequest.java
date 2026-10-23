package pl.kaitoudev.tabkeeper.gateway.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank String displayName, @Email String email, @NotBlank @Size(min = 6) String password) {}
