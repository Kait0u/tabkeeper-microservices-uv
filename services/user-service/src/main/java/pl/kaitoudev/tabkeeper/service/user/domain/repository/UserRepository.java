package pl.kaitoudev.tabkeeper.service.user.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.kaitoudev.tabkeeper.service.user.domain.entity.User;


public interface UserRepository extends JpaRepository<User, UUID> {

    User findFirstByCredentialsId(UUID id);

    boolean existsByCredentialsId(UUID id);

    boolean existsByEmail(String email);
}
 