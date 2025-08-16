plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.wordwave"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wordwave"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // ✅ Zego call SDK
    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:3.13.9")

    // ✅ Firebase (only one BOM version)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database")

    // ✅ Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.1.0")

    // ✅ Image libraries
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.squareup.picasso:picasso:2.8")

    // ✅ Cloudinary
    implementation("com.cloudinary:cloudinary-android:2.3.1")

    // ✅ Country Code Picker
    implementation("com.hbb20:ccp:2.5.2")

    // ✅ OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // ✅ AndroidX + Material
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ✅ Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
