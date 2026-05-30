package com.mirea.kt.ribo.notescope.audio.dsp;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.mirea.kt.ribo.notescope.audio.listener.AudioDataListener;

public class AudioRecorder {
    private boolean isRecording = false;
    private final AudioDataListener listener;
    private final int sampleRate;
    private AudioRecord recorder;

    public AudioRecorder(int sampleRate, AudioDataListener listener) {
        this.sampleRate = sampleRate;
        this.listener = listener;
    }

    @SuppressLint("MissingPermission")
    public void startRecording() {
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                channelConfig,
                audioFormat
        );

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
        );

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            return;
        }

        short[] buffer = new short[bufferSize / 2];
        isRecording = true;
        recorder.startRecording();

        while (isRecording) {
            int readBytes = recorder.read(buffer, 0, buffer.length);
            if (readBytes > 0) {
                short[] copy = new short[readBytes];
                System.arraycopy(buffer, 0, copy, 0, readBytes);
                listener.onAudioData(copy);
            }
        }
    }

    public void stopRecording() {
        isRecording = false;
        if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.stop();
            recorder.release();
        }
    }
}
