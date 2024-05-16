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

mavenPublishing {
    coordinates(GROUP, "gradle-plugin", VERSION_NAME)

    pom {
        name.set(DISPLAY_NAME)
        description.set(DESCRIPTION)
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

gradlePlugin {
    plugins {
        create("reportGenPlugin") {
            id = "com.joetr.compose.guard.report.plugin"
            displayName = DISPLAY_NAME
            description = DESCRIPTION
            version = VERSION_NAME
            implementationClass = "com.joetr.compose.guard.ReportGenPlugin"
        }
    }
}
