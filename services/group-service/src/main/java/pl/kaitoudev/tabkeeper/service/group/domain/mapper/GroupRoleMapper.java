package pl.kaitoudev.tabkeeper.service.group.domain.mapper;

import lombok.experimental.UtilityClass;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupRole;
import pl.kaitoudev.tabkeeper.service.group.domain.enums.GroupMembershipRole;

@UtilityClass
public class GroupRoleMapper {
  public GroupRole toProtobuf(GroupMembershipRole groupMembershipRole) {
    return switch (groupMembershipRole) {
      case OWNER -> GroupRole.GROUP_ROLE_OWNER;
      case MEMBER -> GroupRole.GROUP_ROLE_MEMBER;
      default -> GroupRole.GROUP_ROLE_UNSPECIFIED;
    };
  }
}
