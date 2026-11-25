plugins {
    id("tabkeeper.java-library")
}

dependencies {
    // RabbitTemplate, Spring messaging, context, and transaction support
    implementation(libs.spring.boot.rabbitmq)

    // JSON conversion
    implementation(libs.jackson.databind)
}