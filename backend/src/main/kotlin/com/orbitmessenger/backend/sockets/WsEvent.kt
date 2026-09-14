package com.orbitmessenger.backend.sockets

import com.orbitmessenger.backend.models.MessageResponse
import kotlinx.serialization.Serializable

@Serializable
sealed class WsEvent {
    @Serializable
    data class NewMessage(val message: MessageResponse) : WsEvent()
    
    @Serializable
    data class TypingIndicator(val conversationId: String, val userId: String, val isTyping: Boolean) : WsEvent()
    
    @Serializable
    data class MessageStatusUpdate(val messageId: String, val status: String) : WsEvent()

    // WebRTC Signaling
    @Serializable
    data class WebRtcOffer(val senderId: String, val sdp: String) : WsEvent()
    @Serializable
    data class WebRtcAnswer(val senderId: String, val sdp: String) : WsEvent()
    @Serializable
    data class WebRtcIceCandidate(val senderId: String, val candidate: String, val sdpMid: String, val sdpMLineIndex: Int) : WsEvent()
    @Serializable
    data class EndCall(val senderId: String) : WsEvent()
}

@Serializable
sealed class WsAction {
    @Serializable
    data class SendMessage(
        val conversationId: String, 
        val type: String = "TEXT", 
        val content: String,
        val replyToId: String? = null
    ) : WsAction()

    @Serializable
    data class SendTyping(val conversationId: String, val isTyping: Boolean) : WsAction()
    
    @Serializable
    data class UpdateStatus(val messageId: String, val status: String) : WsAction()

    // WebRTC Signaling Actions
    @Serializable
    data class SendWebRtcOffer(val targetUserId: String, val sdp: String) : WsAction()
    @Serializable
    data class SendWebRtcAnswer(val targetUserId: String, val sdp: String) : WsAction()
    @Serializable
    data class SendWebRtcIceCandidate(val targetUserId: String, val candidate: String, val sdpMid: String, val sdpMLineIndex: Int) : WsAction()
    @Serializable
    data class SendEndCall(val targetUserId: String) : WsAction()
}
