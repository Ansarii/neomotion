plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace  = "com.neoninnovationlab.neomotion.adaptivemotion"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    api(project(":core"))
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.core)
    implementation(libs.compose.material3.adaptive)   // currentWindowAdaptiveInfo(), WindowSizeClass
    implementation(libs.window)                        // WindowInfoTracker, FoldingFeature
    implementation(libs.bundles.lifecycle)
    testImplementation(libs.junit)
}
