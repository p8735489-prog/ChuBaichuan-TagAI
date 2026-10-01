plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "Shirakawa.LocalCueWord"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "Shirakawa.LocalCueWord"
        minSdk = 26
        targetSdk = 35
        versionCode = 30604000
        versionName = "3.6.4"
    }

    signingConfigs {
        // 固定 release 签名：不要删除、替换或改成 debug 签名。
        // 只要平台执行 ./gradlew assembleRelease，release APK 就会使用这个 keystore 签名。
        create("fixedRelease") {
            storeFile = file("chubaichuan-fixed-release.keystore")
            storePassword = "chubaichuan_fixed_2026"
            keyAlias = "chubaichuan-fixed-release"
            keyPassword = "chubaichuan_fixed_2026"
        }
    }

    buildTypes {
        debug {
            // 让 debug 包也使用 release 签名，避免签名不一致导致安装失败
            signingConfig = signingConfigs.getByName("fixedRelease")
        }
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
        // -Xcontext-receivers: 旧名，com.kyant.backdrop 用的是这个语法（context(DelegatableNode)）。
        // -Xcontext-parameters: Kotlin 2.0 起改名后的新名字，本工程内部代码用新语法时启用。
        // 同时给出两个，兼容 kyant 库与本工程自身的写法。
        freeCompilerArgs = listOf("-Xcontext-parameters")
    }

    buildFeatures {
        compose = true
    }
    // 旧版本用 kotlinCompilerExtensionVersion 配 Compose Compiler 版本；
    // Kotlin 2.x 改用 org.jetbrains.kotlin.plugin.compose 插件（在 root build.gradle 声明）。
    // 不再需要 composeOptions 块。

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    // BOM 用 2025.10.00（对应 compose 1.9.0，兼容本工程 compileSdk 35 / AGP 8.7.3）。
    // material3 单独覆盖到 1.5.0-alpha10（基于 compose 1.9.1 构建）：
    // 此版本把 LinearWavyProgressIndicator / CircularWavyProgressIndicator / ExperimentalMaterial3ExpressiveApi
    // 设为公开 API（BOM 默认拉取的 1.4.0 里这些仍是 internal，无法访问）。
    // 注意：不能用 1.5.0-alpha20 —— 它基于 compose 1.12.0-alpha03 构建，会拉入
    // runtime-saveable 1.12.0-alpha03 / navigationevent 1.1.0，要求 compileSdk 37 + AGP 9.1，
    // 与本工程工具链不兼容。local-dream 用的是 alpha20 + AGP 9.1.1 + compileSdk 37 全套，
    // 本工程不具备，故退而求其次用 alpha10（公开 API 与 alpha20 完全一致，仅 compose 基线更低）。
    implementation(platform("androidx.compose:compose-bom:2025.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3:1.5.0-alpha10")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    // Offline ONNX inference
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.18.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Image loading/decoding helper
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Coil for async image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
}