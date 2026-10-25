package pl.kaitoudev.tabkeeper.service.group.domain.mapper;

import lombok.experimental.UtilityClass;
import pl.kaitoudev.tabkeeper.proto.group.v1.GroupMember;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.GroupMembership;
import pl.kaitoudev.tabkeeper.shared.grpc.GrpcMappingUtils;

@UtilityClass
public class GroupMemberMapper {

  public GroupMember toProtobuf(GroupMembership groupMembership) {
    return GroupMember.newBuilder()
        .setUserId(groupMembership.getUserId().toString())
        .setRole(GroupRoleMapper.toProtobuf(groupMembership.getRole()))
        .setJoinedTs(GrpcMappingUtils.toProtobufTimestamp(groupMembership.getJoinedTimestamp()))
        .build();
  }
}
