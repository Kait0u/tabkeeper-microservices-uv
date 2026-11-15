package pl.kaitoudev.tabkeeper.service.expense.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.ExpenseGroup;

public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, UUID> {}
