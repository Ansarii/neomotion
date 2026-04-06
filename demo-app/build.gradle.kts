plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace   = "com.neoninnovationlab.neomotion.demo"
    compileSdk  = 36

    defaultConfig {
        applicationId  = "com.neoninnovationlab.neomotion.demo"
        minSdk         = 26
        targetSdk      = 36                     // API 36: mandatory predictive back + edge-to-edge
        versionCode    = 1
        versionName    = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":morphback"))
    implementation(project(":livejourney"))
    implementation(project(":adaptivemotion"))
    implementation(project(":identitymotion"))

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.core)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Navigation + Adaptive
    implementation(libs.navigation.compose)
    implementation(libs.compose.material3.adaptive)
    implementation(libs.window)

    // Lifecycle + ViewModel
    implementation(libs.bundles.lifecycle)

    // Activity (enableEdgeToEdge — mandatory for API 36)
    implementation(libs.activity.compose)

    // Image loading
    implementation(libs.coil.compose)

    // DI
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
}
