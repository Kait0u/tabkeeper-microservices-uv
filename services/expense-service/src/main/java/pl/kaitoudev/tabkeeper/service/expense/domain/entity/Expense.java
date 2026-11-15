package pl.kaitoudev.tabkeeper.service.expense.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/** An expense paid by a group member. */
@Entity
@Table(
    name = "expenses",
    uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "sequence"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

  @Id
  @Column(nullable = false, updatable = false)
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @Column(name = "paying_user_id", nullable = false)
  private UUID payingUserId;

  @Column(nullable = false, precision = 17, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(nullable = false)
  private Long sequence;

  @Column(name = "created_ts", nullable = false)
  @Builder.Default
  private Instant createdTs = Instant.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private ExpenseGroup group;

  @OneToMany(mappedBy = "expense")
  @Builder.Default
  private Set<ExpenseAllocation> allocations = new HashSet<>();
}
