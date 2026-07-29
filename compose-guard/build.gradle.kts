@file:Suppress("ktlint:standard:property-naming")

import org.gradle.plugins.signing.Sign
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

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

subprojects {
    val hasSigningCredentials = project.hasProperty("signing.keyId") || project.hasProperty("signingInMemoryKey")
    tasks.withType<Sign>().configureEach {
        onlyIf { hasSigningCredentials }
    }
    plugins.withId("signing") {
        configure<SigningExtension> {
            isRequired = hasSigningCredentials
        }
    }
    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            ktlint()
            indentWithSpaces()
            endWithNewline()
            licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        }

        format("misc") {
            target("*.md", ".gitignore", "*.xml", "*.gradle")
            trimTrailingWhitespace()
            endWithNewline()
        }

        kotlinGradle {
            ktlint()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}