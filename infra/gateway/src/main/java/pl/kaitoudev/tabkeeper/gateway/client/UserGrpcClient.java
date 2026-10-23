package pl.kaitoudev.tabkeeper.gateway.client;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.gateway.domain.user.UserDetails;
import pl.kaitoudev.tabkeeper.proto.user.v1.CreateUserRequest;
import pl.kaitoudev.tabkeeper.proto.user.v1.GetUserRequest;
import pl.kaitoudev.tabkeeper.proto.user.v1.UserInfo;
import pl.kaitoudev.tabkeeper.proto.user.v1.UserServiceGrpc;
import pl.kaitoudev.tabkeeper.shared.grpc.GrpcMappingUtils;

@Component
@RequiredArgsConstructor
public class UserGrpcClient {

  private final UserServiceGrpc.UserServiceBlockingStub stub;

  public UserDetails getUser(String userId) {
    GetUserRequest request = GetUserRequest.newBuilder().setUserId(userId).build();

    UserInfo response = stub.withDeadlineAfter(Duration.ofSeconds(3)).getUser(request);

    return UserDetails.builder()
        .userId(UUID.fromString(response.getUserId()))
        .displayName(response.getDisplayName())
        .email(response.getEmail())
        .createdAt(GrpcMappingUtils.fromProtobufTimestamp(response.getCreatedTs()))
        .build();
  }

  public UserDetails createUser(String displayName, String email, String plainPassword) {
    CreateUserRequest request =
        CreateUserRequest.newBuilder()
            .setDisplayName(displayName)
            .setEmail(email)
            .setPassword(plainPassword)
            .build();

    UserInfo response = stub.withDeadlineAfter(Duration.ofSeconds(5)).createUser(request);

    return UserDetails.builder()
        .userId(UUID.fromString(response.getUserId()))
        .displayName(displayName)
        .email(email)
        .createdAt(GrpcMappingUtils.fromProtobufTimestamp(response.getCreatedTs()))
        .build();
  }
}
