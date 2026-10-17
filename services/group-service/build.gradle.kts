plugins {
    id("tabkeeper.spring-application")
}

dependencies {
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.cloud.config)
    implementation(libs.spring.boot.grpc.server)

    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.actuator.test)
    testImplementation(libs.spring.boot.grpc.server.test)
}