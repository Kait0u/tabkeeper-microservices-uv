package pl.kaitoudev.tabkeeper.service.notification.email.renderer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.event.expense.ExpenseCreatedEvent;
import pl.kaitoudev.tabkeeper.event.expense.ExpenseParticipant;
import pl.kaitoudev.tabkeeper.event.expense.RepaymentCreatedEvent;
import pl.kaitoudev.tabkeeper.service.notification.client.model.UserContactDetails;
import pl.kaitoudev.tabkeeper.service.notification.domain.exception.InvalidNotificationEventException;

/** Renders role-specific email templates for expense and repayment events. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpenseTemplateRenderer {
  private final GenericTemplateRenderer genericTemplateRenderer;

  /** Renders an expense receipt for the payer. */
  public String renderExpenseCreatedForPayerEmail(
      ExpenseCreatedEvent event, String groupName, Map<UUID, UserContactDetails> contacts) {
    log.info("Rendering payer email for ExpenseCreatedEvent (EventID={})", event.getEventId());

    Map<String, Object> variables =
        Map.ofEntries(
            Map.entry("groupName", groupName),
            Map.entry("payingUserDisplayName", contacts.get(event.getPayingUserId()).displayName()),
            Map.entry("amount", event.getAmount()),
            Map.entry("currency", event.getCurrency()),
            Map.entry("participants", toParticipantVariables(event, contacts)));

    return genericTemplateRenderer.render("mail/expense-created-payer", variables);
  }

  /** Renders an allocation-specific expense notification for one participant. */
  public String renderExpenseCreatedEmail(
      ExpenseCreatedEvent event,
      String groupName,
      Map<UUID, UserContactDetails> contacts,
      UUID recipientUserId) {
    log.info(
        "Rendering participant email for ExpenseCreatedEvent (EventID={})", event.getEventId());

    BigDecimal allocatedAmount =
        event.getParticipants().stream()
            .filter(participant -> participant.userId().equals(recipientUserId))
            .map(ExpenseParticipant::allocatedAmount)
            .findFirst()
            .orElseThrow(
                () ->
                    InvalidNotificationEventException.ofMessage(
                        "Expense %s has no allocation for participant %s."
                            .formatted(event.getExpenseId(), recipientUserId)));

    Map<String, Object> variables =
        Map.ofEntries(
            Map.entry("groupName", groupName),
            Map.entry("recipientDisplayName", contacts.get(recipientUserId).displayName()),
            Map.entry("recipientAllocatedAmount", allocatedAmount),
            Map.entry("amount", event.getAmount()),
            Map.entry("currency", event.getCurrency()),
            Map.entry("participants", toParticipantVariables(event, contacts)));

    return genericTemplateRenderer.render("mail/expense-created", variables);
  }

  /** Renders the repayment confirmation sent to the repayer. */
  public String renderRepaymentCreatedForRepayerEmail(
      RepaymentCreatedEvent event, String groupName, String recipientDisplayName) {
    log.info("Rendering repayer email for RepaymentCreatedEvent (EventID={})", event.getEventId());

    Map<String, Object> variables =
        Map.ofEntries(
            Map.entry("groupName", groupName),
            Map.entry("recipientDisplayName", recipientDisplayName),
            Map.entry("amount", event.getAmount()),
            Map.entry("currency", event.getCurrency()));

    return genericTemplateRenderer.render("mail/repayment-created-repayer", variables);
  }

  /** Renders the repayment notification sent to the recipient. */
  public String renderRepaymentCreatedEmail(
      RepaymentCreatedEvent event,
      String groupName,
      String repayingUserDisplayName,
      String recipientDisplayName) {
    log.info(
        "Rendering recipient email for RepaymentCreatedEvent (EventID={})", event.getEventId());

    Map<String, Object> variables =
        Map.ofEntries(
            Map.entry("groupName", groupName),
            Map.entry("repayingUserDisplayName", repayingUserDisplayName),
            Map.entry("recipientDisplayName", recipientDisplayName),
            Map.entry("amount", event.getAmount()),
            Map.entry("currency", event.getCurrency()));

    return genericTemplateRenderer.render("mail/repayment-created", variables);
  }

  /** Maps participant shares to variables shared by the payer and participant templates. */
  private List<Map<String, Object>> toParticipantVariables(
      ExpenseCreatedEvent event, Map<UUID, UserContactDetails> contacts) {
    return event.getParticipants().stream()
        .map(
            participant ->
                Map.<String, Object>of(
                    "displayName", contacts.get(participant.userId()).displayName(),
                    "allocatedAmount", participant.allocatedAmount()))
        .toList();
  }
}
