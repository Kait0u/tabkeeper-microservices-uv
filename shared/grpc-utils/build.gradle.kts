plugins {
    id("tabkeeper.java-library")
}

val protobufVersion = "4.33.2"

dependencies {
    implementation("com.google.protobuf:protobuf-java:${protobufVersion}")

    implementation(libs.spring.boot.grpc.client)
    implementation(libs.grpc.inprocess)
}