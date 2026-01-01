plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.freight24.core.notifications"
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
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Material 3 Extended IconSet
    implementation("androidx.compose.material:material-icons-extended:1.x.x")

}
