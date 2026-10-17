plugins {
    id("tabkeeper.spring-application")
}

dependencies {
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.cloud.gateway)
    implementation(libs.spring.cloud.config)

    implementation(libs.spring.boot.grpc.client)

    testImplementation(libs.spring.boot.actuator.test)
    testImplementation(libs.spring.boot.grpc.client.test)
}