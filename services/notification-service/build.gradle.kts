plugins {
    id("tabkeeper.spring-application")
}

dependencies {
    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.cloud.config)
    implementation(libs.spring.boot.grpc.server)

    implementation(libs.spring.boot.web.server)
    implementation(libs.spring.cloud.netflix.eureka.client)

    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.actuator.test)
    testImplementation(libs.spring.boot.grpc.server.test)
}