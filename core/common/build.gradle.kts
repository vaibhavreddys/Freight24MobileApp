plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(17)
}
android {
    namespace = "com.freight24.core.common"
    compileSdk = 36

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
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":core:database"))
//    implementation(project(":core:network"))

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
