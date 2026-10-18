import com.google.protobuf.gradle.*


plugins {
    `java-library`
    id("com.google.protobuf")
}

val protobufVersion = "4.33.2"
val grpcVersion = "1.85.0"
val apacheAnnotationsApiVersion = "11.0.27"


dependencies {
    api("com.google.protobuf:protobuf-java:$protobufVersion")
    api("io.grpc:grpc-protobuf:$grpcVersion")
    api("io.grpc:grpc-stub:$grpcVersion")

    compileOnly("org.apache.tomcat:tomcat-annotations-api:$apacheAnnotationsApiVersion")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }

    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }

    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
            }
        }
    }
}