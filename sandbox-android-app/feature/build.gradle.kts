plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.joetr.compose.guard")
    id("org.jetbrains.kotlin.plugin.compose").version("2.0.0")
    id("com.google.devtools.ksp")
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

composeCompiler.enableStrongSkippingMode.set(true)

dependencies {
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.core:core-ktx:1.13.1")
    ksp("com.bennyhuo.kotlin:deepcopy-compiler-ksp:1.9.20-1.0.1")
    implementation("com.bennyhuo.kotlin:deepcopy-runtime:1.9.20-1.0.1")
}