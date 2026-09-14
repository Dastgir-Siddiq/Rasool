package com.orbitmessenger.feature.calls.webrtc

interface WebRTCClient {
    fun initialize()
    fun startCall(targetUserId: String, isVideo: Boolean)
    fun answerCall()
    fun endCall()
    
    fun toggleMute(isMuted: Boolean)
    fun toggleCamera(isEnabled: Boolean)
    fun switchCamera()

    // Signaling handlers
    fun onRemoteOfferReceived(sdp: String)
    fun onRemoteAnswerReceived(sdp: String)
    fun onRemoteIceCandidateReceived(candidate: String, sdpMid: String, sdpMLineIndex: Int)
}
