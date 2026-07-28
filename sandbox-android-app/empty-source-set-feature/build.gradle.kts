plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.joetr.compose.guard")
    id("org.jetbrains.kotlin.plugin.compose").version("2.0.20")
}

android {
    namespace = "com.example.myapplication.feature"
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.runtime:runtime")
    testImplementation("junit:junit:4.13.2")
}