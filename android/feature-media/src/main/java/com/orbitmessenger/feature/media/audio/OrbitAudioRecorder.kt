package com.orbitmessenger.feature.media.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class OrbitAudioRecorder(private val context: Context) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    override fun startRecording(outputFile: File) {
        currentFile = outputFile
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(outputFile.absolutePath)
            
            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
        }
    }

    override fun cancelRecording() {
        stopRecording()
        currentFile?.let {
            if (it.exists()) {
                it.delete()
            }
        }
        currentFile = null
    }
}
