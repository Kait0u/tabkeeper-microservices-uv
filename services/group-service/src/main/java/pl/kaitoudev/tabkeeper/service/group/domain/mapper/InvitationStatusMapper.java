package pl.kaitoudev.tabkeeper.service.group.domain.mapper;

import lombok.experimental.UtilityClass;
import pl.kaitoudev.tabkeeper.proto.group.v1.InvitationStatus;
import pl.kaitoudev.tabkeeper.service.group.domain.enums.GroupInvitationStatus;

@UtilityClass
public class InvitationStatusMapper {

  public InvitationStatus toProtobuf(GroupInvitationStatus groupInvitationStatus) {
    return switch (groupInvitationStatus) {
      case PENDING -> InvitationStatus.INVITATION_STATUS_PENDING;
      case ACCEPTED -> InvitationStatus.INVITATION_STATUS_ACCEPTED;
      case DECLINED -> InvitationStatus.INVITATION_STATUS_DECLINED;
      case REVOKED -> InvitationStatus.INVITATION_STATUS_REVOKED;
      default -> InvitationStatus.INVITATION_STATUS_UNSPECIFIED;
    };
  }
}
