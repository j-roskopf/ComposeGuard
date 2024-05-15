plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    //id(libs.plugins.mavenPublish.get().pluginId)
    alias(libs.plugins.testkit)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.kotlinx.serialization.json)
}
