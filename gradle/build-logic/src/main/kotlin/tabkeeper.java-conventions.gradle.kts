import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    java
    id("com.diffplug.spotless")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    val lombok = libs.findLibrary("lombok").get()

    compileOnly(lombok)
    annotationProcessor(lombok)
    testCompileOnly(lombok)
    testAnnotationProcessor(lombok)

    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

spotless {
    java {
        googleJavaFormat(libs.findVersion("google-java-format").get().requiredVersion)
        expandWildcardImports()
        shortenFullyQualifiedTypes()
        importOrder()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
