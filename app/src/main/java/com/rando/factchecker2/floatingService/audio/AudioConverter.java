package com.rando.factchecker2.floatingService.audio;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioConverter {
    private com.rando.factchecker2.floatingService.audio.AudioCnovCallback audioCnovCallback;
    private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 8000;
    private static final int CHANNEL_COUNT = 1;
    private static final int BIT_RATE = 64000;
    private static final int BUFFER_SIZE = 1024 * 2;

    /**
     *
     * Constructor for AudioConverter includes a callback interface
     * that will run when the conversion is complete.
     * PCM file settings: 8000 Hz sample rate, mono channel, and 64000 bit rate
     * @param audioCnovCallback Callback interface for when the conversion is complete (AudioCnovCallback)
     */
    public AudioConverter(AudioCnovCallback audioCnovCallback) {
        this.audioCnovCallback = audioCnovCallback;
    }

    /**
     * Convert a PCM file to an AAC file.
     * Input the pcm and the aac files paths to convert.
     * @param pcmFilePath Input PCM file path
     * @param aacFilePath Output AAC file path
     */
    public void convertPcmToAac(String pcmFilePath, String aacFilePath) {
        Thread thread = new Thread(() -> {
            try {
                convertFiles(pcmFilePath, aacFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void convertFiles(String pcmFilePath, String aacFilePath) throws IOException {
        MediaCodec encoder = MediaCodec.createEncoderByType(MIME_TYPE);
        MediaFormat format = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, CHANNEL_COUNT);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, BUFFER_SIZE);

        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.start();

        MediaMuxer muxer = new MediaMuxer(aacFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream inputStream = new FileInputStream(pcmFilePath);
        boolean eos = false;
        int trackIndex = -1;

        while (!eos) {
            int inputIndex = encoder.dequeueInputBuffer(10000);
            if (inputIndex >= 0) {
                ByteBuffer inputBuf = encoder.getInputBuffer(inputIndex);
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    encoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    eos = true;
                } else {
                    inputBuf.clear();
                    inputBuf.put(buffer, 0, bytesRead);
                    encoder.queueInputBuffer(inputIndex, 0, bytesRead, System.nanoTime() / 1000, 0);
                }
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000);
            while (outputIndex >= 0) {
                ByteBuffer outputBuffer = encoder.getOutputBuffer(outputIndex);
                if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                    trackIndex = muxer.addTrack(encoder.getOutputFormat());
                    muxer.start();
                } else {
                    if (trackIndex != -1) {
                        muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo);
                    }
                }
                encoder.releaseOutputBuffer(outputIndex, false);
                outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000);
            }
        }

        encoder.stop();
        encoder.release();
        muxer.stop();
        muxer.release();
        inputStream.close();
        this.audioCnovCallback.onSuccess(new File(aacFilePath));
    }
}

