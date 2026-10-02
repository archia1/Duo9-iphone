plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.fold.iphoneduo"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.fold.iphoneduo"
        minSdk = 33
        targetSdk = 34
        versionCode = 2
        versionName = "2.0-iphone-duo"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.window:window:1.3.0")
    implementation("androidx.window:window-java:1.3.0")
}
