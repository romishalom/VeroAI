package com.rando.factchecker2.floatingService.audio

import com.assemblyai.api.AssemblyAI
import com.assemblyai.api.resources.transcripts.types.Transcript
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

class Transcriber(callback: TranscriberCallback) {
    private val callback: TranscriberCallback = callback;
    public fun transcribeAudio(file: File) {
        val newThread = thread(start = false) {
            val client = AssemblyAI.builder()
                .apiKey("e3c9f2512c104071abbb57f0a74ac5b8")
                .build()
            println("Starting transcription")
            if (client.transcripts().transcribe(file).text.isPresent) {
                val transcript: Transcript = client.transcripts().transcribe(file)
                callback.onSuccess(transcript.text.get())
                println(transcript.text.get())
            } else {
                println("Transcription failed!")
                callback.onFailure(Exception("Transcription failed!"))
                //Study https://stackoverflow.com/questions/11985518/android-record-sound-in-mp3-format
            }
        }

        newThread.start()
        newThread.join()


    }
}