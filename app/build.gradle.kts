plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.molecule)
    kotlin("kapt")
}

android {
    namespace = "social.plasma"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "social.plasma"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.nostr)

    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.hilt.navigationcompose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    implementation(libs.androidx.room.common)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.room.runtime)

    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.workmanager)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.materialicons)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.ui)
    implementation(libs.compose.ui.uitoolingpreview)

    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.ui)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.workmanager)
    kapt(libs.hilt.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodelcompose)

    implementation(libs.moshi.core)
    implementation(libs.moshi.kotlin)

    implementation(libs.okhttp.core)
    implementation(libs.okhttp.interceptor.logging)

    implementation(libs.rxjava)
    implementation(libs.secp256k1.android)

    implementation(libs.scarlet.core)
    implementation(libs.scarlet.lifecycle.android)
    implementation(libs.scarlet.messageadapter.moshi)
    implementation(libs.scarlet.streamadapter.coroutines)
    implementation(libs.scarlet.streamadapter.rxjava)
    implementation(libs.scarlet.websocket.okhttp)

    implementation(libs.timber)
    implementation(libs.touchimageview)

    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.uitooling)

    testImplementation(libs.androidx.paging.common)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.junit)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)

    coreLibraryDesugaring(libs.android.desugaring)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

android.testOptions {
    unitTests.all {
        it.testLogging {
            showStandardStreams = true

        }
        it.useJUnitPlatform()
    }
}
