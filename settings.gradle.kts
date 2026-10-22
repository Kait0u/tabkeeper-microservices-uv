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

    // Infra
    "infra:gateway",
    "infra:discovery",

    // Microservices
    "services:expense-service",
    "services:group-service",
    "services:notification-service",
    "services:user-service",

    // Shared
    "shared:grpc-utils",
)