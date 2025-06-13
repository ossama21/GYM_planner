import com.google.devtools.ksp.gradle.KspTaskJvm
import java.util.Locale

plugins {
    id("com.google.gms.google-services")
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlinCompose)
}

android {
    namespace = "com.H_Oussama.gymplanner" // Using the capitalized version as per your project structure
    compileSdk = 35 // Updated based on previous error
    
    defaultConfig {
        applicationId = "com.H_Oussama.gymplanner"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "4.5-Close-Beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
        checkReleaseBuilds = false
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
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    // Add explicit source set for generated proto code only
    sourceSets {
        named("main") {
            java.srcDirs("build/generated/source/proto/main/java")
            // Remove KSP directories from sourceSets to avoid conflicts
        }
    }
}

// Protobuf configuration
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Example version, check latest

    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text) // Added explicit text dependency
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.google.material) // For Material Components (used by Material 3 theme adapters)

    // Google Generative AI (Gemini)
    implementation("com.google.ai.client.generativeai:generativeai:0.2.1")

    // Coil for image loading
    implementation(libs.coil.compose)
    implementation("io.coil-kt:coil-compose:2.4.0") // Adding direct dependency if not in libs
    implementation("io.coil-kt:coil-gif:2.4.0") // Add support for GIF loading
    
    // Retrofit for network operations
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.navigation.animation) // Note: Accompanist animation is deprecated, consider androidx.navigation.compose.animation

    // DataStore & Protobuf
    implementation(libs.androidx.datastore.core)
    // implementation(libs.androidx.datastore.preferences) // Only if using Preferences DataStore
    implementation(libs.protobuf.javalite)
    implementation(libs.kotlinx.serialization.json) 

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) 
    ksp(libs.androidx.room.compiler) 

    // Hilt Dependencies
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0") // Update to stable version

    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0") // Hilt integration for WorkManager

    // OkHttp for networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Accompanist for system UI controller (already present)
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// KSP configuration
ksp {
    // Remove Android superclass validation disabling as it may be causing issues
}

// Configure KSP tasks dependencies to avoid validation issues
project.afterEvaluate {
    val kspDebugTask = tasks.named("kspDebugKotlin")
    val kspReleaseTask = tasks.named("kspReleaseKotlin")
    
    // Set proper task dependencies
    kspDebugTask.configure {
        mustRunAfter(kspReleaseTask)
    }
    
    tasks.named("compileDebugKotlin").configure {
        dependsOn(kspDebugTask)
    }
    
    tasks.named("compileReleaseKotlin").configure {
        dependsOn(kspReleaseTask)
    }
}

// Simple configuration to ensure main proto generation tasks run before KSP
project.tasks.withType<com.google.devtools.ksp.gradle.KspTaskJvm>().configureEach {
    // Only add dependency on main proto generation tasks
    val protoMainTasks = listOf(
        "generateDebugProto", 
        "generateReleaseProto"
    )
    
    protoMainTasks.forEach { taskName ->
        project.tasks.findByName(taskName)?.let { protoTask ->
            this.dependsOn(protoTask)
        }
    }
}

// Add this to ensure Hilt generated code is included in builds
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        // Enable Hilt compiler options
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers"
        )
    }
}