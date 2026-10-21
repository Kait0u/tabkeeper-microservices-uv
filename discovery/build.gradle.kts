plugins {
    id("tabkeeper.spring-application")
}

dependencies {
    implementation(libs.spring.cloud.netflix.eureka.server)
}