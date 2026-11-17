package pl.kaitoudev.tabkeeper.service.expense.client.exception;

import java.util.UUID;

/** Indicates that Group Service could not be reached or did not respond before the deadline. */
public class GroupServiceUnavailableException extends RuntimeException {

  /** Creates an exception with the supplied dependency-failure message and cause. */
  public GroupServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }

  /** Creates an exception for a failed attempt to retrieve the supplied group. */
  public static GroupServiceUnavailableException ofGroup(UUID groupId, Throwable cause) {
    return new GroupServiceUnavailableException(
        "Group Service is unavailable while retrieving group %s.".formatted(groupId), cause);
  }
}
