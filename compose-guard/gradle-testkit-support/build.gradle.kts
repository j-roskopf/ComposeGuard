plugins {
  id("org.jetbrains.kotlin.jvm")
  alias(libs.plugins.testkit)
}

kotlin {
  explicitApi()
}

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
  maven(url = "https://plugins.gradle.org/m2/")
}

dependencies {
  api(platform(libs.kotlin.bom))
  api(gradleTestKit())

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.api)
  testImplementation(libs.truth)

  testRuntimeOnly(libs.junit.engine)
}
