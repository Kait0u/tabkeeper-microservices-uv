package pl.kaitoudev.tabkeeper.service.expense.domain.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Repayment;

public interface RepaymentRepository extends JpaRepository<Repayment, UUID> {

  @EntityGraph(attributePaths = {"group"})
  @Query("SELECT r FROM Repayment r WHERE r.group.groupId = :groupId ORDER BY r.sequence")
  List<Repayment> findAllByGroupIdOrdered(@Param("groupId") UUID groupId);
}
