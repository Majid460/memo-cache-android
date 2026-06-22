plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
}
publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            groupId = "com.github.Majid460"
            artifactId = "memo-core"
            version = "1.0.0"

            pom {
                name.set("Memo Core")
                description.set("Pure Kotlin caching core for the Memo library")
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