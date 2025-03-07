plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.rando.factchecker2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rando.factchecker2"
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
    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // add the dependency for the Google AI client SDK for Android
    implementation(libs.generativeai)

    //Web stuff
    implementation(libs.okhttp)
    implementation(libs.jsoup)

    // Required for one-shot operations (to use `ListenableFuture` from Reactive Streams)
    implementation(libs.guava)

    // Required for streaming operations (to use `Publisher` from Guava Android)
    implementation(libs.reactive.streams)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}