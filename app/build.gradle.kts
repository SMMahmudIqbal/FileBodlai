plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseStorePath = providers.gradleProperty("RELEASE_STORE_FILE").orNull
    ?: System.getenv("RELEASE_STORE_FILE")
val releaseStorePassword = providers.gradleProperty("KEYSTORE_PASSWORD").orNull
    ?: System.getenv("KEYSTORE_PASSWORD")
val releaseKeyAlias = providers.gradleProperty("KEY_ALIAS").orNull
    ?: System.getenv("KEY_ALIAS")
val releaseKeyPassword = providers.gradleProperty("KEY_PASSWORD").orNull
    ?: System.getenv("KEY_PASSWORD")
val releaseStoreFile = releaseStorePath?.let { file(it) } ?: file("../bodlai-release-key.jks")
val missingReleaseSigningInputs = listOf(
    "KEYSTORE_PASSWORD" to releaseStorePassword,
    "KEY_ALIAS" to releaseKeyAlias,
    "KEY_PASSWORD" to releaseKeyPassword
).filter { it.second.isNullOrBlank() }.map { it.first }

gradle.taskGraph.whenReady {
    if (allTasks.any { it.name == "assembleRelease" || it.name == "bundleRelease" }) {
        check(missingReleaseSigningInputs.isEmpty() && releaseStoreFile.isFile) {
            "Release signing requires a valid keystore and protected values for: ${missingReleaseSigningInputs.joinToString()}. " +
                "Set RELEASE_STORE_FILE (optional) and the KEYSTORE_PASSWORD, KEY_ALIAS, and KEY_PASSWORD values."
        }
    }
}

android {
    namespace = "com.example.fileconverter"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.fileconverter"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    signingConfigs {
        create("release") {
            storeFile = releaseStoreFile
            storePassword = releaseStorePassword.orEmpty()
            keyAlias = releaseKeyAlias.orEmpty()
            keyPassword = releaseKeyPassword.orEmpty()
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.02")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
