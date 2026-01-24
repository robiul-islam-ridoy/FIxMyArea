import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    //id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fixmyarea"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fixmyarea"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Load sensitive credentials from local.properties (not committed to Git)
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(FileInputStream(localPropertiesFile))
        }
        
        // Cloudinary credentials from local.properties
        buildConfigField(
            "String",
            "CLOUDINARY_CLOUD_NAME",
            "\"${properties.getProperty("cloudinary.cloud.name", "")}\""
        )
        buildConfigField(
            "String",
            "CLOUDINARY_UPLOAD_PRESET",
            "\"${properties.getProperty("cloudinary.upload.preset", "")}\""
        )
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
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    // Firebase BoM - manages all Firebase dependency versions
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    
    // Firebase services (versions managed by BoM)
    implementation("com.google.firebase:firebase-auth")           // Authentication
    implementation("com.google.firebase:firebase-firestore")      // Cloud Firestore Database
    implementation("com.google.firebase:firebase-storage")        // Cloud Storage for images
    implementation("com.google.firebase:firebase-messaging")      // Cloud Messaging (Push notifications)
    implementation("com.google.firebase:firebase-analytics")      // Analytics
    
    // Google Play Services for Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Image loading and caching
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // Image picker
    implementation("com.github.dhaval2404:imagepicker:2.1")
    
    // HTTP client for Cloudinary uploads
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // DataStore for session management
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.0.0")
    implementation("io.reactivex.rxjava3:rxjava:3.1.5")

    //Material Components icons
    implementation("com.google.android.material:material:1.11.0")
    
    // OpenStreetMap for Android (osmdroid)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    // Google Play Services Location for GPS
    implementation("com.google.android.gms:play-services-location:21.1.0")
}