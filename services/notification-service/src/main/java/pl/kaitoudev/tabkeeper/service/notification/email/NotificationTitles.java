package pl.kaitoudev.tabkeeper.service.notification.email;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NotificationTitles {
  // User
  public static String USER_CREATED = "Account has been created!";

  // Group & Invite
  public static String INVITATION_CREATED = "Invitation has been sent!";
  public static String INVITATION_ACCEPTED = "Invitation has been accepted!";

  // Expense & Repayment
  public static String EXPENSE_CREATED = "Expense has been recorded!";
  public static String REPAYMENT_CREATED = "Repayment has been recorded!";
}
