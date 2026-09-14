package com.orbitmessenger.feature.media.audio

import java.io.File

interface AudioRecorder {
    fun startRecording(outputFile: File)
    fun stopRecording()
    fun cancelRecording()
}
