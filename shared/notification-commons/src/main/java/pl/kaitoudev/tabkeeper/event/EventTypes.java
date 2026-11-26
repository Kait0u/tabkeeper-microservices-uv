package pl.kaitoudev.tabkeeper.event;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EventTypes {
  public static final String USER_CREATED = "UserCreated";
  public static final String GROUPINVITE_CREATED = "GroupInviteCreated";
  public static final String GROUPINVITE_ACCEPTED = "GroupInviteAccepted";
  public static final String EXPENSE_CREATED = "ExpenseCreated";
  public static final String REPAYMENT_CREATED = "RepaymentCreated";
}
