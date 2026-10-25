package pl.kaitoudev.tabkeeper.service.group.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.GroupMembership;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, UUID> {}
