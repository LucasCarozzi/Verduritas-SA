plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Este plugin es necesario para Firebase
}

android {
    namespace = "lucas.carozzi.verduritassa"
    compileSdk = 34

    defaultConfig {
        applicationId = "lucas.carozzi.verduritassa"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Librerías fundamentales
    implementation(libs.appcompat) // AppCompat
    implementation(libs.material) // Material Components (Asegúrate de tener la versión correcta)
    implementation(libs.activity) // Activity
    implementation(libs.constraintlayout) // ConstraintLayout
    testImplementation(libs.junit) // JUnit para pruebas unitarias
    androidTestImplementation(libs.ext.junit) // JUnit para pruebas en Android
    androidTestImplementation(libs.espresso.core) // Espresso para pruebas UI

    // Firebase (asegúrate de tener el archivo google-services.json en tu proyecto)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1")) // Firebase BOM
    implementation("com.google.firebase:firebase-analytics") // Firebase Analytics
    implementation("com.google.firebase:firebase-auth:23.1.0") // Firebase Auth
    implementation("com.google.firebase:firebase-firestore:25.1.1") // Firestore para base de datos
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Firebase Google Sign-In

    // Librerías adicionales que puedes necesitar
    implementation("com.google.android.material:material:1.4.0") // Material Design (en caso que no esté definida en el archivo de versión)
}
