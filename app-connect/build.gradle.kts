import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

// Set the BASE_URL in your "local.properties" file at Freight24MobileApp/local.properties
val debugBaseUrl = localProperties.getProperty("BASE_URL") ?: "https://backendapi-qhpo.onrender.com/api/v1/"

android {
    namespace = "com.freight24.connect"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.freight24.connect"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            buildConfigField("String", "BASE_URL", "\"${debugBaseUrl}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            //buildConfigField("String", "BASE_URL", "\"https://backendapi-qhpo.onrender.com/api/v1/\"")
        }
    }

    flavorDimensions += listOf("env")

    productFlavors {

        create("qa") {
            dimension = "env"
            buildConfigField(
                "String",
                "BASE_URL",
                "\"http://192.168.1.6:3000/api/v1/\"",
            )
        }

        create("prod") {
            dimension = "env"
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://api.freight24.com\"",
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:auth"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))
    implementation(project(":core:rest"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended:1.x.x")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.firebase.analytics)

    // Coil
    implementation(libs.coil.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Pdf Viewer For Android (Lightweight Compose PDF Viewer)
    implementation(libs.pdf.viewer)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
