plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.neoninnovationlab.neomotion.identitymotion"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core"))

    // Compose (for potential UX utilities if needed later, but mostly for coroutines)
    implementation(libs.coroutines.android)

    // Credential Manager for Restore Credentials API
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
}
