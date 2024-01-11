plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
//    id("java-library")
//    id("org.shipkit.android-jdk-http")
}

android {
    namespace = "maratische.android.sharediscountapps"
    compileSdk = 34

    defaultConfig {
        applicationId = "maratische.android.sharediscountapps"
        minSdk = 30
        targetSdk = 34
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/NOTICE.markdown"
        }
    }
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {

    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-graphics:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation(libs.material)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

//    implementation(files("libs/gpsoauth-1.0.2-marat.jar", "libs/gkeepapi-1.2.6-marat.jar"))
    implementation("io.github.openfeign:feign-core:13.1")
    implementation("io.github.openfeign:feign-gson:13.1")
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation("com.google.zxing:core:3.4.1")

    //telegram
//    implementation("com.github.xonixx:telegram-bot-common:0.1.2")
//    implementation("com.github.pengrad:java-telegram-bot-api:6.7.0")
//    implementation(libs.telegrambots)

//    implementation("org.telegram:telegrambots:6.8.0")

//    implementation("com.github.pengrad:java-telegram-bot-api:6.9.1")

}