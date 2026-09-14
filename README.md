# Rasool Messenger

An iMessage-inspired modern Android messenger, built from scratch with privacy, security, and a premium user experience in mind.

## Features

- **End-to-End Encryption**: Messages are encrypted locally before leaving the device.
- **Real-Time Communication**: Instant delivery and typing indicators via WebSockets.
- **Offline-First**: Complete local persistence using Room.
- **Rich Media**: Support for images, videos, voice messages, and files.
- **Premium UI**: Modern Jetpack Compose interface with smooth animations and material design.

## Project Status

This repository contains the completed architectural foundation and core implementations for an iMessage-inspired messenger, built out across 13 phases:
- Clean Architecture Android modules (`core`, `feature-auth`, `feature-chat`, `feature-settings`, `feature-media`, `feature-calls`).
- Offline-first Room Database caching.
- Hardware-backed AES-GCM End-to-End Encryption (`core-security`).
- Ktor Backend with JWT Auth, WebSockets, and PostgreSQL (via Exposed).
- WebRTC Signaling pathways.

## Getting Started

Please see [DEVELOPMENT.md](DEVELOPMENT.md) for instructions on setting up your local environment.

## Documentation

- [Architecture](ARCHITECTURE.md)
- [Security Model](SECURITY.md)
- [Database Schema](DATABASE.md)
- [API Reference](API.md)
- [Testing](TESTING.md)

## License
Independent Messaging Platform.
