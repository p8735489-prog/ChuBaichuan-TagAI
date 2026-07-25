plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.kuzulabz.waifutaggercn"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kuzulabz.waifutaggercn"
        minSdk = 26
        targetSdk = 35
        versionCode = 29070000
        versionName = "29.7"
    }

    signingConfigs {
        // 固定 release 签名：本地构建时使用固定 keystore 保证覆盖安装。
        // CI 环境中 keystore 不存在时自动生成临时 debug keystore，确保构建不中断。
        create("fixedRelease") {
            val keystoreFile = file("chubaichuan-fixed-release.keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = "chubaichuan_fixed_2026"
                keyAlias = "chubaichuan-fixed-release"
                keyPassword = "chubaichuan_fixed_2026"
            } else {
                val debugKeystore = file("${System.getProperty("user.home")}/.android/debug.keystore")
                if (!debugKeystore.exists()) {
                    debugKeystore.parentFile?.mkdirs()
                    val keytoolCmd = listOf(
                        "keytool", "-genkeypair",
                        "-alias", "androiddebugkey",
                        "-keypass", "android",
                        "-keystore", debugKeystore.absolutePath,
                        "-storepass", "android",
                        "-keyalg", "RSA", "-keysize", "2048",
                        "-validity", "10000",
                        "-dname", "CN=Android Debug,O=Android,C=US"
                    )
                    ProcessBuilder(keytoolCmd).redirectErrorStream(true).start().waitFor()
                }
                storeFile = debugKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("fixedRelease")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xcontext-receivers")
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
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
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

    // Coil for async image loading
    implementation("io.coil-kt:coil-compose:2.6.0")
}
