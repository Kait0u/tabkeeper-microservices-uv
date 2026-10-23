package pl.kaitoudev.tabkeeper.gateway.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kaitoudev.tabkeeper.gateway.client.UserGrpcClient;
import pl.kaitoudev.tabkeeper.gateway.domain.user.UserDetails;
import pl.kaitoudev.tabkeeper.gateway.dto.user.CreateUserRequest;
import pl.kaitoudev.tabkeeper.gateway.dto.user.CreateUserResponse;
import pl.kaitoudev.tabkeeper.gateway.dto.user.GetUserDetailsResponse;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserGrpcClient userGrpcClient;

  @GetMapping("/{user_id}")
  public ResponseEntity<GetUserDetailsResponse> getUserDetails(
      @PathVariable("user_id") UUID userId) {
    UserDetails userDetails = userGrpcClient.getUser(userId.toString());

    GetUserDetailsResponse responseDto =
        GetUserDetailsResponse.builder()
            .userId(userDetails.userId())
            .displayName(userDetails.displayName())
            .email(userDetails.email())
            .createdAt(userDetails.createdAt())
            .build();

    return ResponseEntity.ok(responseDto);
  }

  @PostMapping
  public ResponseEntity<CreateUserResponse> createUser(
      @Valid @RequestBody CreateUserRequest request) {
    UserDetails userDetails =
        userGrpcClient.createUser(request.displayName(), request.email(), request.password());

    URI location = URI.create("/api/v1/users/" + userDetails.userId());
    CreateUserResponse responseDto =
        CreateUserResponse.builder()
            .userId(userDetails.userId())
            .displayName(userDetails.displayName())
            .email(userDetails.email())
            .createdAt(userDetails.createdAt())
            .build();

    return ResponseEntity.created(location).body(responseDto);
  }
}
