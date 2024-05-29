import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val GROUP: String by project
val VERSION_NAME: String by project

plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    alias(libs.plugins.spotless)
    alias(libs.plugins.compatibility.validator)
    alias(libs.plugins.mavenPublish) apply false
}

group = GROUP
version = VERSION_NAME

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("${layout.buildDirectory}/**/*.kt")
            targetExclude("bin/**/*.kt")

            ktlint()
            licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        }
    }
}