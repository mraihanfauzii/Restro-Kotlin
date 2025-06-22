plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.mraihanfauzii.restrokotlin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mraihanfauzii.restrokotlin"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // CameraX
    implementation(libs.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // MediaPipe Tasks – PoseLandmarker
    implementation(libs.tasks.vision)
    implementation(libs.tasks.core)

    // JSON (untuk serialisasi plannedExercises)
    implementation(libs.squareup.moshi)
    implementation(libs.com.squareup.moshi.moshi.kotlin)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Save dark and light themes with datastore
    implementation (libs.androidx.datastore.preferences.core)
    implementation (libs.androidx.datastore.preferences)

    // ROOM DAO penyimpanan internal
    implementation (libs.androidx.room.runtime)
    annotationProcessor (libs.androidx.room.compiler)
    androidTestImplementation (libs.androidx.room.testing)
    implementation (libs.androidx.room.ktx)
    kapt (libs.androidx.room.compiler)

    // Viewmodel
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    implementation (libs.androidx.lifecycle.common.java8)
    implementation (libs.androidx.lifecycle.viewmodel.savedstate)

    // Glide
    implementation (libs.glide)
    annotationProcessor (libs.compiler)

    // Circle Profile
    implementation(libs.circleimageview)

    // Bottom navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.squareup.moshi)
    implementation(libs.com.squareup.moshi.moshi.kotlin)
    kapt(libs.moshi.kotlin.codegen)

    // Tensorflow for CNN
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Just for Chat Feature
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx")
}