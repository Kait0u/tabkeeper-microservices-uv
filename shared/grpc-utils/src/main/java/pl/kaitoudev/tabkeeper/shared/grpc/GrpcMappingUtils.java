package pl.kaitoudev.tabkeeper.shared.grpc;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GrpcMappingUtils {
  public Timestamp toProtobufTimestamp(Instant instant) {
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  public Instant fromProtobufTimestamp(Timestamp timestamp) {
    return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
  }
}
