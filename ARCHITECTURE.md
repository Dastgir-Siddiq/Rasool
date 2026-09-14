# Architecture Overview

Orbit Messenger uses a clean, multi-module architecture for both Android and Backend.

## Android Architecture

The Android app follows Clean Architecture principles and MVVM pattern, heavily relying on Kotlin Coroutines and Flows.

### Module Structure
- `app`: Application entry point and DI setup.
- `core`: Base classes, utilities, and common models.
- `core-ui`: Reusable Compose UI components and theme.
- `core-network`: Network client, WebSocket managers, and API definitions.
- `core-database`: Room database, DAOs, and local entity definitions.
- `core-security`: Encryption keys management, Keystore access, and E2EE implementations.
- `feature-*`: Isolated feature modules (e.g., `feature-auth`, `feature-home`, `feature-chat`, `feature-settings`).

### Data Flow
`UI (Compose) -> ViewModel -> Use Case (Domain) -> Repository -> Data Source (Local/Remote)`

We enforce a strict offline-first strategy:
1. UI subscribes to Local Database via Flow.
2. Actions (e.g., Send Message) write to the local DB immediately (status: queued).
3. Background workers / Real-time sync attempt to push changes to Remote.
4. Remote updates confirm status, which writes to DB and naturally updates UI via Flow.

## Backend Architecture

Built with Kotlin and Ktor, following a standard layered architecture.

- **Routing / API Layer**: REST endpoints and WebSocket connections.
- **Service Layer**: Business logic, authorization checks, and validation.
- **Repository / Data Access Layer**: Interacting with PostgreSQL using Exposed or JDBC.
- **Storage**: Object storage integration for media files.

### Real-Time Delivery
Clients connect via WebSockets. The server maintains active sessions. When a message is sent, the server routes it to the active WebSocket session(s) of the recipient, or relies on Push Notifications (FCM) if disconnected.
