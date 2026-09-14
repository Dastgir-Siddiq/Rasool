# Testing Guide

This project includes both Android and Backend tests.

## Android Tests
- **Unit Tests**: Run with `./gradlew testDebugUnitTest`
- **UI Tests**: Run with `./gradlew connectedDebugAndroidTest`

The Android app uses standard JUnit, MockK (for mocking), and Compose Test Rule for UI interactions.

## Backend Tests
- Run with `./gradlew test`

The backend uses `ktor-server-tests` to mock HTTP requests and test routing logic. We also utilize Testcontainers for verifying PostgreSQL interactions.
