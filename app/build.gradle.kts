plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.kuzulabz.waifutaggercn"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kuzulabz.waifutaggercn"
        minSdk = 26
        targetSdk = 34
        versionCode = 26072179
        versionName = "26.7.21.7.9"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Sign the release build with the auto-generated debug keystore
            // so the APK that comes out of CI is directly installable
            // without you needing to create/manage a real signing key.
            // Fine for personal/testing use; swap in a real signingConfig
            // before any kind of public distribution.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    // The ONNX model file is NOT bundled here (too large / not something
    // I can download in this sandbox). Put your .onnx model file into
    // app/src/main/assets/model.onnx and the tag list into
    // app/src/main/assets/selected_tags.csv before building.
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    // Offline ONNX inference
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.18.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Image loading/decoding helper
    implementation("androidx.exifinterface:exifinterface:1.3.7")
}
