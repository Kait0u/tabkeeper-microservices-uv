plugins {
    id("tabkeeper.spring-application")
}

dependencies {
    implementation(project(":contracts:user-contracts"))
    implementation(project(":shared:grpc-utils"))

    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.cloud.config)

    implementation(libs.spring.boot.web.server)
    implementation(libs.spring.cloud.netflix.eureka.client)

    implementation(libs.spring.boot.grpc.server)
    implementation(libs.spring.data.jpa)
    implementation(libs.flyway)

    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.postgresql)

    testImplementation(libs.spring.boot.actuator.test)
    testImplementation(libs.spring.boot.grpc.server.test)
    testImplementation(libs.spring.data.jpa.test)
    testImplementation(libs.flyway.test)

    // Testcontainers
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
}