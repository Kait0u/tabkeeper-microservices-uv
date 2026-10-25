package pl.kaitoudev.tabkeeper.service.group.domain.mapper;

import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import pl.kaitoudev.tabkeeper.proto.group.v1.PerUserGroupInfo;
import pl.kaitoudev.tabkeeper.service.group.domain.entity.Group;
import pl.kaitoudev.tabkeeper.shared.grpc.GrpcMappingUtils;

@UtilityClass
public class GroupInfoMapper {

  public PerUserGroupInfo groupToGroupInfo(Group group, Predicate<Group> ownershipPredicate) {
    return PerUserGroupInfo.newBuilder()
        .setGroupId(group.getId().toString())
        .setGroupName(group.getName())
        .setIsOwner(ownershipPredicate.test(group))
        .setCreatedTs(GrpcMappingUtils.toProtobufTimestamp(group.getCreatedTimestamp()))
        .build();
  }
}
