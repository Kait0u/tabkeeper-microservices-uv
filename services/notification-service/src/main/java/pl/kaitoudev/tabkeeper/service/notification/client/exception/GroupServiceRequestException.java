package pl.kaitoudev.tabkeeper.service.notification.client.exception;

import java.util.UUID;

/** Indicates that Group Service rejected or failed a request for an unexpected reason. */
public class GroupServiceRequestException extends RuntimeException {

  /** Creates an exception with the supplied dependency-failure message and cause. */
  public GroupServiceRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  /** Creates an exception for an unsuccessful request concerning the supplied group. */
  public static GroupServiceRequestException ofGroup(UUID groupId, Throwable cause) {
    return new GroupServiceRequestException(
        "Group Service failed to retrieve group %s.".formatted(groupId), cause);
  }
}
