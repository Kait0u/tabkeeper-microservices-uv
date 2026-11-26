package pl.kaitoudev.tabkeeper.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.kaitoudev.tabkeeper.config.NotificationMessagingConfig;
import pl.kaitoudev.tabkeeper.event.AbstractEvent;

@RequiredArgsConstructor
public class NotificationEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  public void publishAfterCommit(AbstractEvent event, String routingKey) {
    if (TransactionSynchronizationManager.isActualTransactionActive()
        && TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              publish(event, routingKey);
            }
          }
      );
      return;
    }

    publish(event, routingKey);
  }

  private void publish(AbstractEvent event, String routingKey) {
    rabbitTemplate.convertAndSend(NotificationMessagingConfig.EXCHANGE_NAME, routingKey, event);
  }
}
