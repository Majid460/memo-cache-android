// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0" apply false
}