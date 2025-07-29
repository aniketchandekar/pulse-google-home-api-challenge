plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
}

android {
    namespace = "com.example.googlehomeapisampleapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.googlehomeapisampleapp"
        minSdk = 29
        targetSdk = 34
        versionCode = 32
        versionName = "0.2.12"
        // Google Cloud Project ID used for authentication and Home API access
        buildConfigField("String", "GOOGLE_CLOUD_PROJECT_ID", "\"449111297489\"")
        // Gemini API key for AI-powered therapy suggestions
        buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Library dependencies:
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    // Material Icons Extended for more icon options
    implementation("androidx.compose.material:material-icons-extended")
    
    // Room database for local storage
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Gson for JSON serialization in Room
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Gemini AI for intelligent therapy suggestions
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    
    // Home API SDK dependency:
    implementation(libs.play.services.home)
    implementation("com.google.android.gms:play-services-home-types:17.0.0")
}
