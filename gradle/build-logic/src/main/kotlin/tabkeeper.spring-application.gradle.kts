plugins {
    id("tabkeeper.java-conventions")
    id("org.springframework.boot")
}

val libs = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

dependencies {
    val bootBom = libs.findLibrary("spring-boot-bom").get()
    val cloudBom = libs.findLibrary("spring-cloud-bom").get()

    implementation(platform(bootBom))
    implementation(platform(cloudBom))

    testImplementation(platform(bootBom))
    testImplementation(platform(cloudBom))

    annotationProcessor(platform(bootBom))
    testAnnotationProcessor(platform(bootBom))
}