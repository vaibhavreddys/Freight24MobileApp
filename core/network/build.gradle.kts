plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.freight24.core.network"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 1. Enable the Dimension
    flavorDimensions += "env"

    // 2. Define the matching flavors
    productFlavors {
        create("qa") {
            dimension = "env"
            // Define QA URL here so the Network Module owns it
            buildConfigField("String", "BASE_URL", "\"https://qa.api.freight24.com\"")
        }
        create("prod") {
            dimension = "env"
            // Define Prod URL here
            buildConfigField("String", "BASE_URL", "\"https://api.freight24.com\"")
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"https://backendapi-qhpo.onrender.com/api/v1/\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://freight24.in/api/v1/\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Network
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
