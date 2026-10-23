package pl.kaitoudev.tabkeeper.gateway.service.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kaitoudev.tabkeeper.gateway.client.UserGrpcClient;

@Service
@RequiredArgsConstructor
public class UserIntegrationService {
  private final UserGrpcClient userGrpcClient;
}
