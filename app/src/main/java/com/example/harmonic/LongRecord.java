package com.example.harmonic;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    private static final String TAG = "LongRecord";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_record);

        btnRecord = findViewById(R.id.buttonRecord);
        btnRecord.setOnClickListener(v -> {
            try {
                if(!playing) {

                    if (!btnRecord.isSelected()) {

                        if (checkPermissions()) {
                            recording = true;
                            Log.v(TAG, "Have permission");
                            btnRecord.setSelected(!btnRecord.isSelected());
                            File internalStorageDir = getFilesDir(); // Get internal storage directory
                            audioSaveFile = new File(internalStorageDir, "recordingAudio.ogg");
                            mediaRecorder = new MediaRecorder();
                            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.OGG);
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS);
                            mediaRecorder.setOutputFile(audioSaveFile);
                            mediaRecorder.setOnErrorListener((mr, what, extra) -> {
                                Log.e(TAG, "MediaRecorder error: what=" + what + ", extra=" + extra);
                            });

                            try {
                                mediaRecorder.prepare();
                                mediaRecorder.start();
                                Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
                                Log.v(TAG, "Sound Recorded is in: " + audioSaveFile.getAbsolutePath());
                            } catch (IOException e) {
                                Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
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
                        mediaRecorder.stop();
                        mediaRecorder.reset();
                        mediaRecorder.release();
                        Toast.makeText(LongRecord.this, "Recording stopped", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(this, "Can't record when playing the last recording", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Log.e(TAG, "error recording", e);  // Log the error for debugging
            }
        });

        btnPlay = findViewById(R.id.buttonPlay);
        btnPlay.setOnClickListener(v -> {
            if(!recording) {

                if (!btnPlay.isSelected()) {

                    if (audioSaveFile != null) {
                        mediaPlayer = new MediaPlayer();
                        try {
                            btnPlay.setSelected(!btnPlay.isSelected());
                            playing = true;
                            mediaPlayer.setDataSource(audioSaveFile.getAbsolutePath());
                            mediaPlayer.prepare();
                            Toast.makeText(LongRecord.this, "Sound start to play", Toast.LENGTH_SHORT).show();
                            mediaPlayer.start();
                            Log.e(TAG, "Sound Playing is from: " + audioSaveFile);

                            mediaPlayer.setOnCompletionListener(mp -> {
                                // Reset and release the media player after playback finishes
                                mediaPlayer.reset();
                                mediaPlayer.release();
                                btnPlay.setSelected(!btnPlay.isSelected());
                                playing = false;
                                mediaPlayer = null;
                                Toast.makeText(LongRecord.this, "Sound stopped playing", Toast.LENGTH_SHORT).show();
                            });

                        } catch (IOException e) {
                            playing = false;
                            Toast.makeText(LongRecord.this, "No sound captured", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    } else {
                        Toast.makeText(LongRecord.this, "Please record something first", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    btnPlay.setSelected(!btnPlay.isSelected());
                    playing = false;
                    mediaPlayer = null;
                    Toast.makeText(LongRecord.this, "Sound stopped playing", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Can't play will recording", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext = findViewById(R.id.buttonRight);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LongRecord.this,Counter.class));
            }
        });

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
