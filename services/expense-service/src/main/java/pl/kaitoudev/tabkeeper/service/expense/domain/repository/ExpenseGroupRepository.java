package pl.kaitoudev.tabkeeper.service.expense.domain.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.ExpenseGroup;

/** Manages local coordination records for group financial transactions. */
public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, UUID> {

  /** Creates a coordination row if this group has no financial activity yet. */
  @Modifying
  @Query(
      value = "INSERT INTO expense_groups (group_id) VALUES (:groupId) ON CONFLICT DO NOTHING",
      nativeQuery = true)
  void ensureExists(@Param("groupId") UUID groupId);

  /** Locks a group's coordination row to serialize sequence allocation and repayment validation. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT expenseGroup FROM ExpenseGroup expenseGroup WHERE expenseGroup.groupId = :groupId")
  Optional<ExpenseGroup> findByGroupIdForUpdate(@Param("groupId") UUID groupId);
}
