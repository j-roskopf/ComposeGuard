val GROUP: String by project
val POM_NAME: String by project
val VERSION_NAME: String by project
val POM_DESCRIPTION: String by project

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.testkit)
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven(url = "https://plugins.gradle.org/m2/")
}

group = GROUP
version = VERSION_NAME

dependencies {
    compileOnly(gradleApi())
    compileOnly(kotlin("stdlib"))
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)

    implementation(project(":core"))

    functionalTestImplementation(libs.junit)
    functionalTestImplementation(libs.assertk)
    //functionalTestImplementation(libs.testkit.support)
    // todo joer remove if changes get upstreamed - https://github.com/autonomousapps/dependency-analysis-gradle-plugin/pull/1187
    functionalTestImplementation(project(":gradle-testkit-support"))
    functionalTestImplementation(project(":gradle-plugin"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("reportGenPlugin") {
            id = "com.joetr.compose.guard"
            displayName = POM_NAME
            description = POM_DESCRIPTION
            implementationClass = "com.joetr.compose.guard.ReportGenPlugin"
            tags.set(listOf("android", "compose", "report", "jetpackcompose", "composecompiler"))
        }
    }
}
