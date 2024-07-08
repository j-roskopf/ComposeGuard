plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.joetr.compose.guard")
    id("org.jetbrains.kotlin.plugin.compose").version("2.0.0")
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}