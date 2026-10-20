package pl.kaitoudev.tabkeeper.service.user.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import pl.kaitoudev.tabkeeper.proto.user.v1.CreateUserRequest;
import pl.kaitoudev.tabkeeper.proto.user.v1.CreateUserResponse;
import pl.kaitoudev.tabkeeper.proto.user.v1.GetUserRequest;
import pl.kaitoudev.tabkeeper.proto.user.v1.GetUserResponse;
import pl.kaitoudev.tabkeeper.proto.user.v1.UserServiceGrpc;
import pl.kaitoudev.tabkeeper.service.user.domain.entity.User;
import pl.kaitoudev.tabkeeper.service.user.domain.exception.EmailAlreadyTakenException;
import pl.kaitoudev.tabkeeper.service.user.domain.service.UserService;
import pl.kaitoudev.tabkeeper.shared.grpc.GrpcMappingUtils;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

  private final UserService userService;

  @Override
  public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
    String id = request.getUserId();

    Optional<User> optUser = userService.getUserById(id);

    if (optUser.isEmpty()) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription("User %s not found.".formatted(id))
              .asRuntimeException());

      responseObserver.onCompleted();
      return;
    }

    User user = optUser.get();

    GetUserResponse response =
        GetUserResponse.newBuilder()
            .setUserId(user.getId().toString())
            .setDisplayName(user.getDisplayName())
            .setEmail(user.getEmail())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void createUser(
      CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {

    String displayName = request.getDisplayName();
    String email = request.getEmail();
    String plainPassword = request.getPassword();

    User user;
    try {
      user = userService.createUser(displayName, email, plainPassword);
    } catch (EmailAlreadyTakenException ex) {
      responseObserver.onError(Status.ALREADY_EXISTS.withCause(ex).asRuntimeException());

      responseObserver.onCompleted();
      return;
    }

    CreateUserResponse response =
        CreateUserResponse.newBuilder()
            .setUserId(user.getId().toString())
            .setDisplayName(displayName)
            .setEmail(email)
            .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(user.getCreatedTimestamp()))
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
