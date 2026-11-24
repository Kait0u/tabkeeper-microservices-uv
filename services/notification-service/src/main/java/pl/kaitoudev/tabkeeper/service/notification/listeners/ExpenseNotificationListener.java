package pl.kaitoudev.tabkeeper.service.notification.listeners;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pl.kaitoudev.tabkeeper.event.expense.ExpenseCreatedEvent;
import pl.kaitoudev.tabkeeper.event.expense.ExpenseParticipant;
import pl.kaitoudev.tabkeeper.event.expense.RepaymentCreatedEvent;
import pl.kaitoudev.tabkeeper.service.notification.client.model.UserContactDetails;
import pl.kaitoudev.tabkeeper.service.notification.email.NotificationTitles;
import pl.kaitoudev.tabkeeper.service.notification.email.renderer.ExpenseTemplateRenderer;
import pl.kaitoudev.tabkeeper.service.notification.service.NotificationContactResolver;
import pl.kaitoudev.tabkeeper.service.notification.service.NotificationService;

/** Handles financial events with role-specific emails for every concerned party. */
@Slf4j
@Component
public class ExpenseNotificationListener extends AbstractNotificationListener {
  private final ExpenseTemplateRenderer expenseTemplateRenderer;
  private final NotificationContactResolver contactResolver;

  public ExpenseNotificationListener(
      NotificationService notificationService,
      ExpenseTemplateRenderer expenseTemplateRenderer,
      NotificationContactResolver contactResolver) {
    super(notificationService);
    this.expenseTemplateRenderer = expenseTemplateRenderer;
    this.contactResolver = contactResolver;
  }

  /** Sends a receipt to the payer and an allocation notification to each other participant. */
  @RabbitListener(queues = "expense.created")
  public void handleExpenseCreated(ExpenseCreatedEvent event) {
    log.info("Received ExpenseCreated event {}", event.getEventId());

    String groupName = contactResolver.resolveGroupName(event.getGroupId());
    Set<UUID> contactIds = new LinkedHashSet<>();
    contactIds.add(event.getPayingUserId());
    event.getParticipants().forEach(participant -> contactIds.add(participant.userId()));
    Map<UUID, UserContactDetails> contacts = contactResolver.resolveUsers(contactIds);

    UserContactDetails payer = contacts.get(event.getPayingUserId());
    String payerHtml =
        expenseTemplateRenderer.renderExpenseCreatedForPayerEmail(event, groupName, contacts);
    log.info("Sending expense receipt to payer {} - {}...", payer.userId(), payer.email());
    notificationService.sendNotification(
        payer.email(), NotificationTitles.EXPENSE_CREATED, payerHtml);

    for (ExpenseParticipant participant : event.getParticipants()) {
      if (participant.userId().equals(event.getPayingUserId())) {
        continue;
      }

      UserContactDetails participantContact = contacts.get(participant.userId());
      String participantHtml =
          expenseTemplateRenderer.renderExpenseCreatedEmail(
              event, groupName, contacts, participant.userId());
      log.info(
          "Sending allocation email to participant {} - {}...",
          participantContact.userId(),
          participantContact.email());
      notificationService.sendNotification(
          participantContact.email(), NotificationTitles.EXPENSE_CREATED, participantHtml);
    }

    log.info("ExpenseCreated event {} handled.", event.getEventId());
  }

  /** Sends different repayment versions to the repayer and the recipient. */
  @RabbitListener(queues = "repayment.created")
  public void handleRepaymentCreated(RepaymentCreatedEvent event) {
    log.info("Received RepaymentCreatedEvent event {}", event.getEventId());

    String groupName = contactResolver.resolveGroupName(event.getGroupId());
    Map<UUID, UserContactDetails> contacts =
        contactResolver.resolveUsers(
            List.of(event.getRepayingUserId(), event.getRecipientUserId()));
    UserContactDetails repayer = contacts.get(event.getRepayingUserId());
    UserContactDetails recipient = contacts.get(event.getRecipientUserId());

    String repayerHtml =
        expenseTemplateRenderer.renderRepaymentCreatedForRepayerEmail(
            event, groupName, recipient.displayName());
    log.info(
        "Sending repayment confirmation to repayer {} - {}...", repayer.userId(), repayer.email());
    notificationService.sendNotification(
        repayer.email(), NotificationTitles.REPAYMENT_CREATED, repayerHtml);

    String recipientHtml =
        expenseTemplateRenderer.renderRepaymentCreatedEmail(
            event, groupName, repayer.displayName(), recipient.displayName());
    log.info(
        "Sending repayment notification to recipient {} - {}...",
        recipient.userId(),
        recipient.email());
    notificationService.sendNotification(
        recipient.email(), NotificationTitles.REPAYMENT_CREATED, recipientHtml);

    log.info("RepaymentCreated event {} handled.", event.getEventId());
  }
}
