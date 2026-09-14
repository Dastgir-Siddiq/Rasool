# Database Schema

Orbit Messenger uses Room (SQLite) on Android and PostgreSQL on the backend.

## Core Entities

### Users
- `id` (UUID)
- `username` (String, unique)
- `display_name` (String)
- `avatar_url` (String, optional)
- `public_key` (String/Bytes)

### Conversations
- `id` (UUID)
- `type` (Enum: DIRECT, GROUP)
- `name` (String, optional for group)
- `created_at` (Timestamp)

### Conversation Members
- `conversation_id` (UUID)
- `user_id` (UUID)
- `role` (Enum: MEMBER, ADMIN)

### Messages
- `id` (UUID)
- `conversation_id` (UUID)
- `sender_id` (UUID)
- `type` (Enum: TEXT, IMAGE, VIDEO, AUDIO, FILE, SYSTEM)
- `content` (String, encrypted payload)
- `status` (Enum: SENDING, SENT, DELIVERED, READ, FAILED)
- `created_at` (Timestamp)
- `reply_to_id` (UUID, optional)

### Message Attachments
- `id` (UUID)
- `message_id` (UUID)
- `media_type` (String, MIME type)
- `url` (String)
- `size` (Long)
- `thumbnail_url` (String, optional)
