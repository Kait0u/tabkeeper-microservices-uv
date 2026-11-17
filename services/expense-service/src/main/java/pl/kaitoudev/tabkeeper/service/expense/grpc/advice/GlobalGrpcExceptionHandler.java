package pl.kaitoudev.tabkeeper.service.expense.grpc.advice;

import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.advice.GrpcAdvice;
import org.springframework.grpc.server.advice.GrpcExceptionHandler;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.InvalidExpenseException;

@Slf4j
@GrpcAdvice
public class GlobalGrpcExceptionHandler {

  /** Reports malformed identifiers and monetary values as invalid client input. */
  @GrpcExceptionHandler({InvalidExpenseException.class, IllegalArgumentException.class})
  public Status handleInvalidRequest(RuntimeException ex) {
    log.warn("Invalid expense request: {}", ex.getMessage());

    return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
  }

  /** Prevents unexpected failures from exposing implementation details to clients. */
  @GrpcExceptionHandler(Exception.class)
  public Status handleUnexpected(Exception ex) {
    log.error("Unhandled gRPC service exception", ex);

    return Status.INTERNAL.withDescription("An internal service error occurred.");
  }
}
