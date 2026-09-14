# API Reference

## REST API (Base: `/api/v1`)

### Authentication
- `POST /auth/register` - Create a new account
- `POST /auth/login` - Authenticate and receive tokens
- `POST /auth/refresh` - Refresh access token
- `POST /auth/logout` - Invalidate session

### Users
- `GET /users/me` - Get current user profile
- `PUT /users/me` - Update profile
- `GET /users/search?q={query}` - Search users by username

### Conversations
- `GET /conversations` - List user's conversations
- `POST /conversations` - Create a direct or group conversation
- `GET /conversations/{id}` - Get conversation details
- `GET /conversations/{id}/messages` - Fetch paginated message history

### Media
- `POST /media/upload` - Securely upload an attachment (returns URL)

## Real-Time WebSocket API (Base: `/ws`)

- **Connect**: `wss://{host}/ws/chat?token={access_token}`
- **Events**:
  - `MessageEvent`: Incoming new message
  - `StatusEvent`: Message status update (delivered, read)
  - `TypingEvent`: User is typing
  - `PresenceEvent`: User came online/offline
