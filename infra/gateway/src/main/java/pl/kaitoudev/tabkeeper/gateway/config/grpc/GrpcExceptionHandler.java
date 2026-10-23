package pl.kaitoudev.tabkeeper.gateway.config.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GrpcExceptionHandler {

  @ExceptionHandler(StatusRuntimeException.class)
  public ProblemDetail handleGrpc(StatusRuntimeException ex) {
    Status.Code code = ex.getStatus().getCode();

    HttpStatus status =
        switch (code) {
          case NOT_FOUND -> HttpStatus.NOT_FOUND;
          case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
          case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
          case ALREADY_EXISTS -> HttpStatus.CONFLICT;
          case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
          case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
          case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
          case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
          default -> HttpStatus.BAD_GATEWAY;
        };

    ProblemDetail problemDetail = ProblemDetail.forStatus(status);

    problemDetail.setDetail(status.getReasonPhrase());
    problemDetail.setDetail(
        switch (code) {
          case NOT_FOUND -> "The requested resource was not found.";
          case INTERNAL -> "Internal Server Error.";
          case INVALID_ARGUMENT -> "The request is invalid.";
          case ALREADY_EXISTS -> "The requested resource already exists.";
          case UNAUTHENTICATED -> "Authentication is required.";
          case PERMISSION_DENIED -> "Access denied.";
          case DEADLINE_EXCEEDED -> "The upstream service timed out.";
          case UNAVAILABLE -> "The upstream service is unavailable.";
          default -> "An upstream service error has occurred.";
        });

    return problemDetail;
  }
}
