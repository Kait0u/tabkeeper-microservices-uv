package pl.kaitoudev.tabkeeper.service.expense.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.kaitoudev.tabkeeper.service.expense.client.GroupGrpcClient;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Expense;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.ExpenseAllocation;
import pl.kaitoudev.tabkeeper.service.expense.domain.exception.InvalidExpenseException;
import pl.kaitoudev.tabkeeper.service.expense.domain.model.GroupDetails;
import pl.kaitoudev.tabkeeper.service.expense.domain.repository.ExpenseAllocationRepository;
import pl.kaitoudev.tabkeeper.service.expense.domain.repository.ExpenseGroupRepository;
import pl.kaitoudev.tabkeeper.service.expense.domain.repository.ExpenseRepository;

/** Verifies expense persistence against the real PostgreSQL schema and JPA mappings. */
@SpringBootTest(
    properties = {
      "eureka.client.enabled=false",
      "spring.cloud.discovery.enabled=false",
      "spring.grpc.server.port=0"
    })
@Testcontainers(disabledWithoutDocker = true)
class ExpenseServiceIntegrationTests {
  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");

  @Autowired private ExpenseService expenseService;
  @Autowired private ExpenseRepository expenseRepository;
  @Autowired private ExpenseAllocationRepository expenseAllocationRepository;
  @Autowired private ExpenseGroupRepository expenseGroupRepository;
  @MockitoBean private GroupGrpcClient groupGrpcClient;

  private UUID groupId;
  private UUID payingUserId;
  private UUID participantId;

  /** Creates a fresh group snapshot and clears financial records before each test. */
  @BeforeEach
  void setUp() {
    expenseRepository.deleteAll();
    expenseGroupRepository.deleteAll();

    groupId = UUID.randomUUID();
    payingUserId = UUID.randomUUID();
    participantId = UUID.randomUUID();
    when(groupGrpcClient.getGroup(groupId))
        .thenReturn(new GroupDetails(groupId, "USD", Set.of(payingUserId, participantId)));
  }

  /** Persists an expense and its shares while allocating the first group-local sequence. */
  @Test
  void createExpensePersistsExpenseAndAllocationsWithGroupSequence() {
    Expense expense =
        expenseService.createExpense(
            groupId,
            payingUserId,
            new BigDecimal("30.00"),
            "USD",
            List.of(allocation(payingUserId, "10.00"), allocation(participantId, "20.00")));

    assertThat(expense.getId()).isNotNull();
    assertThat(expense.getSequence()).isEqualTo(1L);
    assertThat(expenseRepository.findById(expense.getId())).isPresent();
    assertThat(expenseAllocationRepository.findAll())
        .hasSize(2)
        .allSatisfy(
            allocation -> {
              assertThat(allocation.getExpense().getId()).isEqualTo(expense.getId());
              assertThat(allocation.getAmount().scale()).isEqualTo(2);
            });
    assertThat(expenseGroupRepository.findById(groupId))
        .hasValueSatisfying(group -> assertThat(group.getNextSequence()).isEqualTo(2L));
  }

  /** Rejects unbalanced allocations without creating an expense or a coordination row. */
  @Test
  void createExpenseRejectsAllocationsThatDoNotTotalExpenseAmount() {
    assertThatThrownBy(
            () ->
                expenseService.createExpense(
                    groupId,
                    payingUserId,
                    new BigDecimal("30.00"),
                    "USD",
                    List.of(allocation(payingUserId, "10.00"))))
        .isInstanceOf(InvalidExpenseException.class)
        .hasMessageContaining("must total the expense amount");

    assertThat(expenseRepository.count()).isZero();
    assertThat(expenseAllocationRepository.count()).isZero();
    assertThat(expenseGroupRepository.existsById(groupId)).isFalse();
  }

  /** Creates a participant share with an exact two-decimal amount. */
  private static ExpenseAllocation allocation(UUID userId, String amount) {
    return ExpenseAllocation.builder().userId(userId).amount(new BigDecimal(amount)).build();
  }
}
