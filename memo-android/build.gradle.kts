plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
    id("maven-publish")
}

android {
    namespace = "com.majidshahbaz.memo.android"
    compileSdk = 37
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

}

dependencies {
    implementation(project(":memo-core"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.litertlm.android)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    // Ktor
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.core)
    testImplementation(libs.kotlinx.coroutines.test.v180)


}
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.Majid460"
                artifactId = "memo-android"
                version = "1.0.0"

                pom {
                    name.set("Memo Android")
                    description.set("Offline-first AI caching layer for Android")
                    url.set("https://github.com/Majid460/memo-cache-android")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("Majid460")
                            name.set("Majid Shahbaz")
                        }
                    }
                }
            }
        }
    }
}