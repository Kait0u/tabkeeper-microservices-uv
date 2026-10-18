pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenCentral()
    }
}

rootProject.name = "tabkeeper"

include(
    // Contracts
    "contracts:user-contracts",

    // Gateway
    "gateway",

    // Microservices
    "services:expense-service",
    "services:group-service",
    "services:notification-service",
    "services:user-service",
)