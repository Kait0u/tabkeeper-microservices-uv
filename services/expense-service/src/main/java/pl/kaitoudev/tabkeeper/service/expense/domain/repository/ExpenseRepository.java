package pl.kaitoudev.tabkeeper.service.expense.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

  /** Retrieves an expense together with the allocations needed by the gRPC response mapper. */
  @EntityGraph(attributePaths = {"allocations"})
  @Query("SELECT expense FROM Expense expense WHERE expense.id = :expenseId")
  Optional<Expense> findByIdWithAllocations(@Param("expenseId") UUID expenseId);

  /** Retrieves all group expenses with allocations in shared transaction order. */
  @EntityGraph(attributePaths = {"allocations"})
  @Query(
      """
      SELECT DISTINCT expense
      FROM Expense expense
      WHERE expense.group.groupId = :groupId
      ORDER BY expense.sequence
      """)
  List<Expense> findAllByGroupIdWithAllocationsOrdered(@Param("groupId") UUID groupId);

  /** Retrieves expenses paid by a group member, with allocations, in transaction order. */
  @EntityGraph(attributePaths = {"allocations"})
  @Query(
      """
      SELECT DISTINCT expense
      FROM Expense expense
      WHERE expense.group.groupId = :groupId
        AND expense.payingUserId = :payingUserId
      ORDER BY expense.sequence
      """)
  List<Expense> findAllPaidByUserWithAllocations(
      @Param("groupId") UUID groupId, @Param("payingUserId") UUID payingUserId);

  /** Retrieves expenses containing a member's allocation, with all allocations, in order. */
  @EntityGraph(attributePaths = {"allocations"})
  @Query(
      """
      SELECT DISTINCT expense
      FROM Expense expense
      WHERE expense.group.groupId = :groupId
        AND EXISTS (
          SELECT allocation
          FROM ExpenseAllocation allocation
          WHERE allocation.expense = expense
            AND allocation.userId = :allocatedUserId
        )
      ORDER BY expense.sequence
      """)
  List<Expense> findAllAllocatedToUserWithAllocations(
      @Param("groupId") UUID groupId, @Param("allocatedUserId") UUID allocatedUserId);
}
