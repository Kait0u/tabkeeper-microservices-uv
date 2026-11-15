package pl.kaitoudev.tabkeeper.service.expense.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.kaitoudev.tabkeeper.service.expense.domain.entity.Repayment;

public interface RepaymentRepository extends JpaRepository<Repayment, UUID> {}
