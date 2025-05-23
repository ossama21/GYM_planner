// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Other buildscript dependencies (like Kotlin Gradle plugin, AGP) might be here or managed elsewhere
    }
}

plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.protobuf) apply false // Ensure protobuf plugin is declared here if used project-wide
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false // Apply Hilt plugin here
    alias(libs.plugins.kotlinCompose) apply false // Apply Compose plugin here
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

true // Needed to make the build script work. 