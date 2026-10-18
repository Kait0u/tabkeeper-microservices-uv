plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.gradle.plugin)
    implementation(libs.spotless.gradle.plugin)
    implementation(libs.google.protobuf.gradle.plugin)
}