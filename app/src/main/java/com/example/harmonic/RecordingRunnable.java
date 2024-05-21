package com.example.harmonic;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.TextView;

public class RecordingRunnable implements Runnable {

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_8BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final String TAG = "LongRecord";
    private boolean recording;
    private AudioRecord microphone;

    TextView occurrences;

    public RecordingRunnable(TextView occurrences) {
        this.occurrences = occurrences;
    }

    @Override
    public void run() {
        recordAndSend();
    }


    public void recordAndSend() {
        try {
            prepareRecord();

            int sum = 0;
            int previous = 0;
            //Since audio format is 8 bit, we need to create a 8 bit (byte data type) buffer
            byte[] buffer = new byte[BUFFER_SIZE * 100];
            recording=true;
            while (recording && !Thread.interrupted()) {
                //reading audio from buffer
                int readSize = microphone.read(buffer, 0, buffer.length);
                // Send to server the recording and sum the total occurrences
                sum += sendToServer(readSize, buffer);
                if (sum != previous) {
                    previous = sum;
                    if(sum<1000){
                        String messageToScreen = "Number of occurrences: " + sum;
                        Log.v(TAG, messageToScreen);
                        occurrences.setText(messageToScreen);
                    }
                    else{
                        String messageToScreen = "Number of occurrences: +999";
                        occurrences.setText(messageToScreen);
                    }
                }
            }


        } catch (Exception e) {
            Log.e(TAG, "error recording", e);  // Log the error for debugging
        } finally {
            microphone.stop();
            microphone.release();
        }
    }

    public void stop() {
        Log.v(TAG, "Stop!!!!!!!!!");
        recording = false;
    }


    @SuppressLint("MissingPermission")
    public void prepareRecord() {
        microphone = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
        microphone.startRecording();
    }

    public static int sendToServer(int readSize, byte[] buffer) { // send to server sound chunk
        byte[] msg = ("LongRecord" + "~" + MainActivity.getUsername() + "~").getBytes();

        byte[] toSend = new byte[msg.length + readSize + 1];
        System.arraycopy(msg, 0, toSend, 1, msg.length);
        System.arraycopy(buffer, 0, toSend, msg.length + 1, readSize);
        toSend[0] = (byte) msg.length;
        SendRecv.send(MainActivity.getmHandler(), MainActivity.getIp(), toSend);
        String received = SendRecv.receiveData();
        if (received.contains("Number")) {
            return Integer.parseInt(received.split(" ")[3]);
        }
        return 0;
    }


}
