package pl.kaitoudev.tabkeeper.service.expense.grpc.advice;

import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.advice.GrpcAdvice;
import org.springframework.grpc.server.advice.GrpcExceptionHandler;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.InvalidRequestException;

@Slf4j
@GrpcAdvice
public class GlobalGrpcExceptionHandler {

  /** Reports malformed request data and domain validation failures as invalid client input. */
  @GrpcExceptionHandler(InvalidRequestException.class)
  public Status handleInvalidRequest(InvalidRequestException ex) {
    log.warn("Invalid request: {}", ex.getMessage());

    return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
  }

  /** Prevents unexpected failures from exposing implementation details to clients. */
  @GrpcExceptionHandler(Exception.class)
  public Status handleUnexpected(Exception ex) {
    log.error("Unhandled gRPC service exception", ex);

    return Status.INTERNAL.withDescription("An internal service error occurred.");
  }
}
