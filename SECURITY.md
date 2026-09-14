# Security Architecture

Security is a primary concern for Orbit Messenger.

## End-to-End Encryption (E2EE)

*(Note: Phase 1-7 use a well-defined cryptographic abstraction layer. Full E2EE implementation in Phase 8.)*

- **Identity Keys**: Generated on device, stored securely in Android Keystore.
- **Session Keys**: Established using a robust protocol (e.g., Signal Protocol/X3DH).
- **Message Payload**: Encrypted using AES-256-GCM. The backend only sees the ciphertexts and routing metadata.

## Authentication & Authorization

- JWT-based authentication with short-lived access tokens and secure, HTTP-only/encrypted refresh tokens.
- Passwords (if used) are hashed with Argon2id on the backend.
- Devices are independently registered. A single user can have multiple device sessions.

## Data at Rest (Android)

- The Room Database does not encrypt non-sensitive metadata by default (to allow fast search/UI), but the actual message contents can be encrypted at rest using SQLCipher if configured.
- Cryptographic keys are protected by the hardware-backed Android Keystore.

## Network Security

- TLS 1.3 is enforced for all REST and WebSocket connections.
- Certificate pinning is highly recommended for production builds.

## Backend Security Practices

- **Input Validation**: All incoming data is strongly typed and validated.
- **Rate Limiting**: Applied to login, registration, and message sending endpoints.
- **Authorization**: The server independently verifies the sender's right to post to a conversation and the recipient's identity.
