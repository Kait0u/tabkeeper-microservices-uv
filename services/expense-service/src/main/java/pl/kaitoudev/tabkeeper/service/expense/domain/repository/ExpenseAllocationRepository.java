package pl.kaitoudev.tabkeeper.service.expense.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.ExpenseAllocation;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.ExpenseAllocationId;

public interface ExpenseAllocationRepository
    extends JpaRepository<ExpenseAllocation, ExpenseAllocationId> {}
