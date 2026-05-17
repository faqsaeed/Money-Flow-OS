pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "money-flow-os"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
  ":app",
  ":core:common",
  ":core:domain",
  ":core:database",
  ":core:parser",
  ":core:analytics",
  ":core:security",
  ":sms",
  ":feature:dashboard",
  ":feature:transactions",
  ":feature:people",
  ":feature:admin",
  ":feature:session",
)
