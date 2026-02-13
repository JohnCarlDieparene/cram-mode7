import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

// Load COHERE API key from local.properties
val localProperties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}
val cohereApiKey = localProperties.getProperty("COHERE_API_KEY") ?: ""

val cloudName = localProperties.getProperty("YOUR_CLOUD_NAME") ?: ""
val cloudApiKey = localProperties.getProperty("YOUR_API_KEY") ?: ""
val cloudApiSecret = localProperties.getProperty("YOUR_API_SECRET") ?: ""

android {

    namespace = "com.labactivity.crammode"
    compileSdk = 36 // latest compile SDK

    defaultConfig {
        applicationId = "com.labactivity.crammode"
        minSdk = 24
        targetSdk = 35 // keep 35 to avoid new behavior changes
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject COHERE API key
        buildConfigField("String", "COHERE_API_KEY", "\"$cohereApiKey\"")

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudName\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"$cloudApiKey\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"$cloudApiSecret\"")
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

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation("androidx.datastore:datastore-preferences:1.1.0")

    implementation("androidx.compose.material:material:1.6.0")
// or latest stable
    implementation("androidx.compose.material:material-icons-core:1.6.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    implementation("com.google.accompanist:accompanist-flowlayout:0.32.0") // stable version

    implementation("com.google.accompanist:accompanist-swiperefresh:0.31.5-beta")



    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:2.0.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Firebase UI
    implementation("com.firebaseui:firebase-ui-firestore:8.0.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Jetpack Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Compose lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Material & UI
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.media3:media3-common:1.1.1")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ML Kit
    implementation("com.google.mlkit:text-recognition:16.0.0-beta3")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
