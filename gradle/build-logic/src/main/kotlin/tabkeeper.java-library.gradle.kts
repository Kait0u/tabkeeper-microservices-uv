import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("tabkeeper.java-conventions")
    `java-library`
}

val libs = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

val bootBom = libs.findLibrary("spring-boot-bom").get()

dependencies {
    api(platform(bootBom))
    annotationProcessor(platform(bootBom))
    testAnnotationProcessor(platform(bootBom))
}