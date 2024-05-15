val GROUP: String by project
val VERSION_NAME: String by project
val YEAR: String by project
val DISPLAY_NAME: String by project
val DESCRIPTION: String by project
val PROJECT_URL: String by project
val DEVELOPER_USERNAME: String by project
val DEVELOPER_NAME: String by project
val DEVELOPER_URL: String by project
val PROJECT_CONNECTION: String by project
val DEVELOPER_CONNECTION: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    alias(libs.plugins.testkit)
    alias(libs.plugins.mavenPublish)
}

repositories {
    mavenCentral()
}

mavenPublishing {
    coordinates(GROUP, "core", VERSION_NAME)

    pom {
        name.set("Core")
        description.set("Core functionality for Compose Guard")
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
    implementation(kotlin("stdlib"))
    implementation(libs.kotlinx.serialization.json)
}
