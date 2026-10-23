plugins {
    id("tabkeeper.spring-application")
}

dependencies {
    implementation(project(":contracts:user-contracts"))
    implementation(project(":shared:grpc-utils"))

    implementation(libs.spring.boot.actuator)
    implementation(libs.spring.cloud.gateway)
    implementation(libs.spring.cloud.config)

    implementation(libs.spring.boot.web.server)
    implementation(libs.spring.cloud.netflix.eureka.client)

    implementation(libs.spring.boot.grpc.client)
    implementation(libs.grpc.inprocess)

    testImplementation(libs.spring.boot.actuator.test)
    testImplementation(libs.spring.boot.grpc.client.test)
}