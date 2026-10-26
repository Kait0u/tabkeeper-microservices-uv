package pl.kaitoudev.tabkeeper.service.group.grpc.config;

import io.grpc.ServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.GlobalServerInterceptor;
import pl.kaitoudev.tabkeeper.service.group.grpc.interceptor.GrpcLoggingServerInterceptor;

@Configuration
public class GrpcInterceptorConfig {

  @Bean
  @GlobalServerInterceptor
  ServerInterceptor grpcLoggingServerInterceptor() {
    return new GrpcLoggingServerInterceptor();
  }
}
