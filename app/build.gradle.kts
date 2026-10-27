plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "app.byos.android"
    compileSdk = 36
    defaultConfig {
        applicationId = "app.byos.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        debug { applicationIdSuffix = ".debug"; versionNameSuffix = "-sprint0" }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    // Signing material is supplied by the owner's local environment, never source control.
    val uploadStore = providers.environmentVariable("BYOS_UPLOAD_STORE_FILE").orNull
    if (!uploadStore.isNullOrBlank()) {
        val uploadSigning = signingConfigs.create("upload") {
            storeFile = file(uploadStore)
            storePassword = providers.environmentVariable("BYOS_UPLOAD_STORE_PASSWORD").orNull
                ?: error("BYOS_UPLOAD_STORE_PASSWORD is required")
            keyAlias = providers.environmentVariable("BYOS_UPLOAD_KEY_ALIAS").orNull
                ?: error("BYOS_UPLOAD_KEY_ALIAS is required")
            keyPassword = providers.environmentVariable("BYOS_UPLOAD_KEY_PASSWORD").orNull
                ?: error("BYOS_UPLOAD_KEY_PASSWORD is required")
        }
        buildTypes.getByName("release").signingConfig = uploadSigning
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    lint { abortOnError = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.icons)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.media3.player)
    implementation(libs.media3.session)
    debugImplementation(libs.compose.tooling)
    debugImplementation(libs.compose.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.test)
    androidTestImplementation(libs.coroutines.test)
}
