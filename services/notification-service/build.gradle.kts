plugins {
    id("tabkeeper.spring-application")
}

dependencies {
    // Events
    implementation(project(":contracts:user-contracts"))
    implementation(project(":contracts:group-contracts"))
    implementation(project(":shared:events"))
    implementation(project(":shared:grpc-utils"))

    // RabbitMQ
    implementation(libs.spring.boot.rabbitmq)

    // Email
    implementation(libs.spring.boot.mail)
    implementation(libs.spring.boot.thymeleaf)

    // Health
    implementation(libs.spring.boot.actuator)

    // Config
    implementation(libs.spring.cloud.config)

    // Eureka
    implementation(libs.spring.boot.web.server)
    implementation(libs.spring.cloud.netflix.eureka.client)

    // Test
    testImplementation(libs.spring.boot.test)
}