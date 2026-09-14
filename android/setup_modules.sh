#!/bin/bash
set -e

# Base content for Android libraries
LIBRARY_BUILD_GRADLE="plugins {
    id(\"com.android.library\")
    id(\"org.jetbrains.kotlin.android\")
    id(\"com.google.dagger.hilt.android\")
    id(\"com.google.devtools.ksp\")
}
android {
    namespace = \"com.orbitmessenger.%s\"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = \"androidx.test.runner.AndroidJUnitRunner\"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = \"17\" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = \"1.5.8\" }
}
dependencies {
    implementation(\"androidx.core:core-ktx:1.12.0\")
    implementation(\"androidx.lifecycle:lifecycle-runtime-ktx:2.7.0\")
    implementation(platform(\"androidx.compose:compose-bom:2024.01.00\"))
    implementation(\"androidx.compose.ui:ui\")
    implementation(\"androidx.compose.ui:ui-graphics\")
    implementation(\"androidx.compose.ui:ui-tooling-preview\")
    implementation(\"androidx.compose.material3:material3\")
    
    implementation(\"com.google.dagger:hilt-android:2.50\")
    ksp(\"com.google.dagger:hilt-android-compiler:2.50\")
}
"

# Set up each module
for mod in core core-ui core-network core-database core-security feature-auth feature-chat feature-home; do
    NAMESPACE=$(echo $mod | sed 's/-/_/g')
    
    printf "$LIBRARY_BUILD_GRADLE" "$NAMESPACE" > $mod/build.gradle.kts
    
    cat << MANIFEST > $mod/src/main/AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
MANIFEST
done

# Basic resources
cat << RES > app/src/main/res/values/strings.xml
<resources>
    <string name="app_name">Orbit Messenger</string>
</resources>
RES

cat << THEME > app/src/main/res/values/themes.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.OrbitMessenger" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
THEME

# MainActivity
cat << MAIN > app/src/main/java/com/orbitmessenger/MainActivity.kt
package com.orbitmessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text("Welcome to Orbit Messenger")
                }
            }
        }
    }
}
MAIN

cat << APP > app/src/main/java/com/orbitmessenger/OrbitMessengerApp.kt
package com.orbitmessenger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OrbitMessengerApp : Application()
APP

echo "Modules setup complete"
