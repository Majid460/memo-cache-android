import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.majidshahbaz.memo"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.majidshahbaz.memo"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localProps.load(localPropsFile.inputStream())
        }
        buildConfigField(
            "String",
            "MEMO_MODEL_RESOLVER_ENDPOINT",
            "\"${localProps.getProperty("memo.model.resolver.endpoint", "")}\""
        )
        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"${localProps.getProperty("memo.cloud.api.key", "")}\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":memo-android"))
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material.icons.extended)

    // 2. The ViewModel & Lifecycle Compose Extensions (Crucial)
    // This gives you the `viewModel()` function and lifecycle utilities
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // 3. Activity Integration for Compose
    // Provides ComponentActivity.setContent { ... } to host Compose from standard Activities
    implementation(libs.androidx.activity.compose)

    // Ktor for Cloud Inference
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
}
