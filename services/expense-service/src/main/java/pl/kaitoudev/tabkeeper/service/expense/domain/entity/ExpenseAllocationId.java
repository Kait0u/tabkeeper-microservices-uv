package pl.kaitoudev.tabkeeper.service.expense.domain.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Identifies a participant's allocation within an expense. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ExpenseAllocationId implements Serializable {
  private UUID expense;
  private UUID userId;
}
