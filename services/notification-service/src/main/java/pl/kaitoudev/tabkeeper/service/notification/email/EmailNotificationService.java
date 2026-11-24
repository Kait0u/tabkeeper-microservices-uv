package pl.kaitoudev.tabkeeper.service.notification.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import pl.kaitoudev.tabkeeper.service.notification.exception.NotificationDeliveryException;
import pl.kaitoudev.tabkeeper.service.notification.service.NotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService {
  private final EmailConfig emailConfig;
  private final JavaMailSender mailSender;

  @Override
  public void sendNotification(String recipient, String subject, String htmlBody) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper =
          new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());

      helper.setFrom(emailConfig.getFromEmail());
      helper.setTo(recipient);
      helper.setSubject(subject);
      helper.setText(htmlBody, true);

      log.info("Sending email to {}...", recipient);
      mailSender.send(message);
      log.info("Successfully sent email notification to {}", recipient);
    } catch (MessagingException | MailException e) {
      log.error("Error sending email notification to {}", recipient, e);
      throw new NotificationDeliveryException(
          "Error sending email notification to %s".formatted(recipient), e);
    }
  }
}
