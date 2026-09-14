#!/bin/bash
set -e

# Backend scaffolding
mkdir -p backend/src/main/kotlin/com/orbitmessenger/backend
mkdir -p backend/src/main/resources
mkdir -p backend/gradle/wrapper
touch backend/build.gradle.kts backend/settings.gradle.kts backend/gradle.properties

# Android scaffolding
mkdir -p android/app/src/main/java/com/orbitmessenger
mkdir -p android/app/src/main/res/values
mkdir -p android/core/src/main/java/com/orbitmessenger/core
mkdir -p android/core-ui/src/main/java/com/orbitmessenger/core/ui
mkdir -p android/core-network/src/main/java/com/orbitmessenger/core/network
mkdir -p android/core-database/src/main/java/com/orbitmessenger/core/database
mkdir -p android/core-security/src/main/java/com/orbitmessenger/core/security
mkdir -p android/feature-auth/src/main/java/com/orbitmessenger/feature/auth
mkdir -p android/feature-chat/src/main/java/com/orbitmessenger/feature/chat
mkdir -p android/feature-home/src/main/java/com/orbitmessenger/feature/home

touch android/settings.gradle.kts android/build.gradle.kts android/gradle.properties
touch android/app/build.gradle.kts android/app/src/main/AndroidManifest.xml

# Empty gradle files for modules
for mod in core core-ui core-network core-database core-security feature-auth feature-chat feature-home; do
    touch android/$mod/build.gradle.kts
    touch android/$mod/src/main/AndroidManifest.xml
done

echo "Scaffold complete"
