package pl.kaitoudev.tabkeeper.service.group.grpc.advice;

import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.advice.GrpcAdvice;
import org.springframework.grpc.server.advice.GrpcExceptionHandler;

@Slf4j
@GrpcAdvice
public class GlobalGrpcExceptionHandler {

  @GrpcExceptionHandler(Exception.class)
  public Status handleUnexpected(Exception ex) {
    log.error("Unhandled gRPC service exception", ex);

    return Status.INTERNAL.withDescription("An internal service error occurred.");
  }
}
