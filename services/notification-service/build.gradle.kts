plugins {
    id("tabkeeper.spring-application")
}

dependencies {
    // Events
    implementation(project(":shared:events"))

    // RabbitMQ
    implementation(libs.spring.boot.rabbitmq)

    // Email
    implementation(libs.spring.boot.mail)

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