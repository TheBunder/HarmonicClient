package com.example.harmonic;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class LongRecord extends AppCompatActivity {

    ImageButton btnRecord, btnPlay;
    Button btnNext;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    File audioSaveFile;
    boolean permission;
    boolean recording;
    boolean playing;

    final int sampleRate = 48000;
    final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private static final String TAG = "LongRecord";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_record);

        btnRecord = findViewById(R.id.buttonRecord);
        btnRecord.setOnClickListener(v -> {
            try {
                int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                AudioRecord microphone = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufferSize * 10);
                if (!btnRecord.isSelected()) {

                    if (checkPermissions()) {
                        recording = true;
                        Log.v(TAG, "Have permission");
                        btnRecord.setSelected(!btnRecord.isSelected());

                        microphone.startRecording();


                        try {

                            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
                            int sum = 0;
                            int previos= 0;
                            //Since audioformat is 16 bit, we need to create a 16 bit (short data type) buffer
                            short[] buffer = new short[20000];
                            while (recording) {
                                int readSize = microphone.read(buffer, 0, buffer.length);
                                sum += sendToServer("LongRecord",recording?0:1 ,readSize, buffer);
                                if(sum != previos){
                                    Log.v(TAG, "Number of occurrences: "+ sum);
                                    previos = sum;
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
                            microphone.stop();
                            microphone.release();
                            Log.e(TAG, "error recording", e);  // Log the error for debugging
                            btnRecord.setSelected(!btnRecord.isSelected());
                        }


                    } else {
                        Log.v(TAG, "missing permission");
                        ActivityCompat.requestPermissions(LongRecord.this,
                                new String[]{android.Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO}, 1);
                        permission = true;
                    }

                } else {
                    recording = false;
                    btnRecord.setSelected(!btnRecord.isSelected());
                    microphone.stop();
                    microphone.release();
                    Toast.makeText(LongRecord.this, "Recording stopped", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Log.e(TAG, "error recording", e);  // Log the error for debugging
            }
        });


        btnNext = findViewById(R.id.buttonRight);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LongRecord.this, Counter.class));
            }
        });

    }

    private static int sendToServer(String code, int state, int readSize, short[] buffer) {
        byte[] msg = (code + "~" + MainActivity.getUsername() + "~"+state+"~"+readSize+"~").getBytes();
        byte[] bufferAsBytes = new byte[readSize*2];
        for (int i = 0; i < readSize; ++i)
        {
            bufferAsBytes[2*i] = getByte1(buffer[i]);
            bufferAsBytes[2*i+1] = getByte2(buffer[i]);
        }
        byte[] toSend = new byte[msg.length + readSize*2 + 1];
        System.arraycopy(msg, 0, toSend, 1, msg.length);
        System.arraycopy(bufferAsBytes, 0, toSend, msg.length + 1, readSize*2);
        toSend[0] = (byte) msg.length;
        SendRecv.send(MainActivity.getmHandler(), MainActivity.getIp(), toSend);
        String received = SendRecv.receive_data();
        if(received.contains("Number")){
            return Integer.parseInt(received.split(" ")[3]);
        }
        return 0;
    }
    public static byte getByte1(short s) {
        return (byte)s;
    }

    public static byte getByte2(short s) {
        return (byte)(s>> 8);
    }

    boolean checkPermissions() {
        int recordAudioPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        int readMediaAudioPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_AUDIO);

        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED || readMediaAudioPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LongRecord.this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO}, 1);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, allow recording to start
                btnRecord.setSelected(true);
            } else {
                // Permission denied, inform user
                Toast.makeText(this, "Permissions are required to record audio and save files.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
