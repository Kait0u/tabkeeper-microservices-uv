package pl.kaitoudev.tabkeeper.service.group.grpc.interceptor;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.GlobalServerInterceptor;

@Slf4j
@GlobalServerInterceptor
public class GrpcLoggingServerInterceptor implements ServerInterceptor {

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

    String method = call.getMethodDescriptor().getFullMethodName();
    long start = System.nanoTime();

    log.info("[gRPC IN] --> {}", method);

    ServerCall<ReqT, RespT> loggingCall =
        new SimpleForwardingServerCall<>(call) {

          @Override
          public void sendMessage(RespT message) {
            log.debug("[gRPC OUT RESPONSE] --> {}: {}", method, message);
            super.sendMessage(message);
          }

          @Override
          public void close(Status status, Metadata trailers) {
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            log.info(
                "[gRPC END] --> {} | status={} | duration={}ms",
                method,
                status.getCode(),
                durationMs);

            super.close(status, trailers);
          }
        };

    ServerCall.Listener<ReqT> listener = next.startCall(loggingCall, headers);

    return new SimpleForwardingServerCallListener<>(listener) {

      @Override
      public void onMessage(ReqT message) {
        log.debug("[gRPC IN REQUEST] --> {}: {}", method, message);
        super.onMessage(message);
      }
    };
  }
}
