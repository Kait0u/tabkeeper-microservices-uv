plugins {
    id("tabkeeper.spring-application")
}

dependencies {
    implementation(project(":contracts:user-contracts"))
    implementation(project(":contracts:group-contracts"))
    implementation(project(":contracts:expense-contracts"))
    implementation(project(":shared:grpc-utils"))

    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.cloud.gateway)
    implementation(libs.springdoc.openapi)
    implementation(libs.spring.cloud.config)

    implementation(libs.spring.boot.web.server)
    implementation(libs.spring.cloud.netflix.eureka.client)

    testImplementation(libs.spring.boot.actuator.test)
    testImplementation(libs.spring.boot.grpc.client.test)
}