plugins {
    id("tabkeeper.spring-application")
}

dependencies {
    // Contract
    implementation(project(":contracts:group-contracts"))

    // gRPC
    implementation(libs.spring.boot.grpc.server)
    implementation(project(":shared:grpc-utils"))

    // Database
    implementation(libs.spring.data.jpa)
    implementation(libs.flyway)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.postgresql)

    // Health
    implementation(libs.spring.boot.actuator)

    // Config
    implementation(libs.spring.cloud.config)

    // Eureka
    implementation(libs.spring.boot.web.server)
    implementation(libs.spring.cloud.netflix.eureka.client)

    // Test
    testImplementation(libs.spring.boot.actuator.test)
    testImplementation(libs.spring.boot.grpc.server.test)
    testImplementation(libs.spring.data.jpa.test)
    testImplementation(libs.flyway.test)

    // Testcontainers
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
}