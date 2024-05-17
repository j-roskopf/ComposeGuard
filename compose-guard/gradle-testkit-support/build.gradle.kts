val GROUP: String by project
val VERSION_NAME: String by project
val YEAR: String by project
val DISPLAY_NAME: String by project
val PROJECT_URL: String by project
val DEVELOPER_USERNAME: String by project
val DEVELOPER_NAME: String by project
val DEVELOPER_URL: String by project
val PROJECT_CONNECTION: String by project
val DEVELOPER_CONNECTION: String by project

plugins {
  kotlin("jvm")
  alias(libs.plugins.testkit)
  alias(libs.plugins.mavenPublish)
}

kotlin {
  explicitApi()
}

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
}

mavenPublishing {
  coordinates(GROUP, "test-kit", VERSION_NAME)

  pom {
    name.set("Test Kit")
    description.set("A testing framework for Gradle projects")
    inceptionYear.set(YEAR)
    url.set(PROJECT_URL)
    licenses {
      license {
        name.set("MIT License")
        url.set("https://opensource.org/license/mit")
        distribution.set("https://opensource.org/license/mit")
      }
    }
    developers {
      developer {
        id.set(DEVELOPER_USERNAME)
        name.set(DEVELOPER_NAME)
        url.set(DEVELOPER_URL)
      }
    }
    scm {
      url.set(PROJECT_URL)
      connection.set(PROJECT_CONNECTION)
      developerConnection.set(DEVELOPER_CONNECTION)
    }
  }
}

dependencies {
  api(platform(libs.kotlin.bom))
  api(gradleTestKit())

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.api)
  testImplementation(libs.truth)
}
