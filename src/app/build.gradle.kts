plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.roamwise"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.roamwise"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { viewBinding = true }

    // 🔧 Make Java compile to 1.8 bytecode
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 🔧 Make Kotlin compile to 1.8 bytecode
    kotlinOptions {
        jvmTarget = "11"
    }
}

// (optional but good): use JDK 21 toolchain to run Gradle/Kotlin, bytecode still 1.8
kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.10")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
