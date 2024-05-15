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
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.testkit)
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven(url = "https://plugins.gradle.org/m2/")
}

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
            id = GROUP
            displayName = DISPLAY_NAME
            description = DESCRIPTION
            version = VERSION_NAME
            implementationClass = "com.joetr.compose.guard.ReportGenPlugin"
        }
    }
}
