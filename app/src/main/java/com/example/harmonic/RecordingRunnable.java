package com.example.harmonic;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.annotation.Nullable;

public class RecordingRunnable implements Runnable {

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private static final String TAG = "LongRecord";

    private boolean recording = true;
    private AudioRecord microphone;

    @Override
    public void run() {
        recordAndSend();
    }

    public void recordAndSend() {
        try {
            AudioTrack audioTrack = prepareRecord();
            if (audioTrack == null) return;


            int sum = 0;
            int previous = 0;
            //Since audio format is 16 bit, we need to create a 16 bit (short data type) buffer
            short[] buffer = new short[BUFFER_SIZE * 40];


            while (recording) {
                //reading audio from buffer
                int readSize = microphone.read(buffer, 0, buffer.length);
                //playing that audio simultaneously
                audioTrack.write(buffer, 0, buffer.length);

                sum += sendToServer(recording ? 0 : 1, readSize, buffer);
                if (sum != previous) {
                    Log.v(TAG, "Number of occurrences: " + sum);
                    previous = sum;
                }
            }
        } catch (Exception e) {
//            Toast.makeText(longRecord, "Recording failed", Toast.LENGTH_SHORT).show();

            Log.e(TAG, "error recording", e);  // Log the error for debugging
        } finally {
            microphone.stop();
            microphone.release();
            //change from record to idol (stop record)
        }

    }

    public void stop() {
        Log.v(TAG, "Stop!!!!!!!!!");
        recording = false;
    }


    @SuppressLint("MissingPermission")
    @Nullable
    public AudioTrack prepareRecord() {
//        //int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
//        if (ContextCompat.checkSelfPermission(longRecord, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return null;
//        }
        microphone = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

        microphone.startRecording();

        //For playing audio
//                        AudioTrack audioTrack = new AudioTrack.Builder()
//                                .setAudioAttributes(AudioAttributes.CONTENT_TYPE_UNKNOWN)
//                                .setAudioFormat(AudioFormat.CHANNEL_OUT_MONO)
//                                .setAudioFormat(AudioFormat.ENCODING_OPUS)
//                                .setBufferSizeInBytes(bufferSize)
//                                .build();
        AudioTrack audioTrack = new AudioTrack(AudioAttributes.CONTENT_TYPE_UNKNOWN,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE,
                AudioTrack.MODE_STREAM);

        audioTrack.setPlaybackRate(SAMPLE_RATE);
        audioTrack.play();
        return audioTrack;
    }

    public static int sendToServer(int state, int readSize, short[] buffer) {
        byte[] msg = ("LongRecord" + "~" + MainActivity.getUsername() + "~" + state + "~" + readSize + "~").getBytes();
        byte[] bufferAsBytes = new byte[readSize * 2];
        for (int i = 0; i < readSize; ++i) {
            bufferAsBytes[2 * i] = getByte1(buffer[i]);
            bufferAsBytes[2 * i + 1] = getByte2(buffer[i]);
        }
        byte[] toSend = new byte[msg.length + readSize * 2 + 1];
        System.arraycopy(msg, 0, toSend, 1, msg.length);
        System.arraycopy(bufferAsBytes, 0, toSend, msg.length + 1, readSize * 2);
        toSend[0] = (byte) msg.length;
        SendRecv.send(MainActivity.getmHandler(), MainActivity.getIp(), toSend);
        String received = SendRecv.receive_data();
        if (received.contains("Number")) {
            return Integer.parseInt(received.split(" ")[3]);
        }
        return 0;
    }

    public static byte getByte1(short s) {
        return (byte) s;
    }

    public static byte getByte2(short s) {
        return (byte) (s >> 8);
    }


}
