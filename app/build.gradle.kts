import org.apache.tools.ant.util.JavaEnvUtils.VERSION_1_8

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")

}

android {
    namespace = "com.ruchitech.carlanuchertab"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ruchitech.carlanuchertab"
        minSdk = 26
        targetSdk = 34
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
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.coil.compose)
    implementation(libs.material)
    implementation(libs.gson)
    implementation(libs.kotlinx.datetime)
  implementation (libs.snowfall)
  //  implementation(libs.kotlinx.serialization.json)
    // Room components
    implementation (libs.androidx.room.runtime)
    kapt (libs.androidx.room.compiler)
    implementation(libs.kotlinx.metadata.jvm)
// Optional - Kotlin Extensions and Coroutines support
    implementation (libs.androidx.room.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.palette.ktx)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation ("androidx.hilt:hilt-work:1.0.0")
    kapt("com.google.dagger:hilt-compiler:2.55")
    implementation("com.google.dagger:hilt-android:2.55")
    implementation("androidx.work:work-runtime:2.10.2")
    implementation(libs.androidx.material3.window.size.class1)
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    implementation("androidx.navigation:navigation-compose:2.9.1")

}
// Add this to enable annotation processing with Hilt
kapt {
    correctErrorTypes = true
}