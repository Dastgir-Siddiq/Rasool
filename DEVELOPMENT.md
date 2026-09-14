# Development Guide

## Prerequisites
- Android Studio Ladybug or later (for Android)
- JDK 17
- IntelliJ IDEA (for Backend)
- Docker & Docker Compose (for Postgres/Redis)

## Backend Setup
1. Navigate to `/backend`.
2. Copy `.env.example` to `.env` and fill in secrets.
3. Run `docker-compose up -d` to start the PostgreSQL database.
4. Run `./gradlew run` to start the Ktor development server.

## Android Setup
1. Open the `/android` directory in Android Studio.
2. In `local.properties`, configure the backend URL (e.g., `API_BASE_URL="http://10.0.2.2:8080"`).
3. Sync Gradle and run the `app` module on an emulator or physical device.

## Phased Development Process
We are building this iteratively:
- Phase 1: Structure & Architecture
- Phase 2: Authentication
- Phase 3: DB & Local Message Model
- Phase 4: Backend APIs
... (see requirements)
