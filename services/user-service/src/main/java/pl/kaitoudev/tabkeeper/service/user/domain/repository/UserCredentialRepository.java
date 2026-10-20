package pl.kaitoudev.tabkeeper.service.user.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.kaitoudev.tabkeeper.service.user.domain.entity.UserCredential;

public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {}
