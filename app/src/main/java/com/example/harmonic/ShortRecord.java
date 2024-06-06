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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import android.app.AlertDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;


public class ShortRecord extends AppCompatActivity {

    ImageButton btnRecord;
    ImageButton btnPlay;
    Button btnNext;
    Button btnSave;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    File audioSaveFile;
    boolean permission;
    boolean recording;
    boolean playing;

    private static final String TAG = "ShortRecord";

    private List<String> savedRecordingNames; // List to store recording names
    private ArrayAdapter<String> spinnerAdapter; // Adapter for the spinner


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_record);

        // Initialize saved recording names list
        savedRecordingNames = new ArrayList<>();
        savedRecordingNames.add("default");
        String[] sounds = getSounds().split("~");
        savedRecordingNames.addAll(Arrays.stream(sounds).filter(sound -> !sound.isEmpty()).collect(Collectors.toList()));

        // Create the ArrayAdapter with an empty list initially
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, savedRecordingNames);

        // Set the adapter for the spinner
        Spinner soundSelector = findViewById(R.id.sound_selector);
        soundSelector.setAdapter(spinnerAdapter);

        btnRecord = findViewById(R.id.buttonRecord);
        btnRecord.setOnClickListener(v -> {
            try {
                if (!playing) {// check if the app plays a recording

                    if (!btnRecord.isSelected()) {// check whether to stop or to start

                        if (checkPermissions()) {// check if there are needed premision
                            recording = true;
                            Log.v(TAG, "Have permission");
                            switchOnRecorder();
                            soundSelector.setSelection(0);
                            File internalStorageDir = getFilesDir(); // Get internal storage directory
                            audioSaveFile = new File(internalStorageDir, "recordingAudio.ogg");
                            mediaRecorder = new MediaRecorder();
                            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.OGG);
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS);
//                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                            mediaRecorder.setAudioSamplingRate(48000);
                            mediaRecorder.setAudioChannels(1);
                            mediaRecorder.setOutputFile(audioSaveFile);
                            mediaRecorder.setOnErrorListener((mr, what, extra) ->
                                    Log.e(TAG, "MediaRecorder error: what=" + what + ", extra=" + extra)
                            );

                            try {
                                mediaRecorder.prepare();
                                mediaRecorder.start();
                                Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
                                Log.v(TAG, "Sound Recorded is in: " + audioSaveFile.getAbsolutePath());
                            } catch (IOException e) {
                                Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "error recording", e);  // Log the error for debugging
                                switchOffRecorder();
                            }

                        } else {// ask for premision
                            Log.v(TAG, "missing permission");
                            ActivityCompat.requestPermissions(ShortRecord.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO}, 1);
                            permission = true;
                        }

                    } else {
                        recording = false;
                        switchOffRecorder();
                        mediaRecorder.stop();
                        mediaRecorder.reset();
                        mediaRecorder.release();
                        Toast.makeText(ShortRecord.this, "Recording stopped", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Can't record when playing the last recording", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "error recording", e);  // Log the error for debugging
            }
        });

        btnPlay = findViewById(R.id.buttonPlay);
        btnPlay.setOnClickListener(v -> {
            if (!recording) {// check if the app record

                if (!btnPlay.isSelected()) {// whether to start or to stop the recording

                    if (audioSaveFile != null) {// check if the user recorded something
                        mediaPlayer = new MediaPlayer();
                        try {
                            switchOnPlayer();
                            playing = true;
                            mediaPlayer.setDataSource(audioSaveFile.getAbsolutePath());
                            mediaPlayer.prepare();
                            Toast.makeText(ShortRecord.this, "Sound start to play", Toast.LENGTH_SHORT).show();
                            mediaPlayer.start();
                            Log.e(TAG, "Sound Playing is from: " + audioSaveFile);

                            mediaPlayer.setOnCompletionListener(mp -> {
                                // Reset and release the media player after playback finishes
                                mediaPlayer.reset();
                                mediaPlayer.release();
                                switchOffPlayer();
                                playing = false;
                                mediaPlayer = null;
                                Toast.makeText(ShortRecord.this, "Sound stopped playing", Toast.LENGTH_SHORT).show();
                            });

                        } catch (IOException e) {
                            playing = false;
                            Toast.makeText(ShortRecord.this, "No sound captured", Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    } else {
                        Toast.makeText(ShortRecord.this, "Please record something first", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    switchOffPlayer();
                    playing = false;
                    mediaPlayer = null;
                    Toast.makeText(ShortRecord.this, "Sound stopped playing", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Can't play will recording", Toast.LENGTH_SHORT).show();
            }
        });


        btnNext = findViewById(R.id.buttonRight);
        btnNext.setOnClickListener(v -> {// send the selection (by name or by file) and move to the next screen
            if (Objects.equals(soundSelector.getSelectedItem(), "default")) {
                try (BufferedInputStream fis = new BufferedInputStream(Files.newInputStream(audioSaveFile.toPath()))) {
                    long current = 0;
                    long fileLength = audioSaveFile.length();
                    byte[] contents;
                    while (current != fileLength) {
                        int size = 10000;
                        if (fileLength - current >= size)
                            current += size;
                        else {
                            size = (int) (fileLength - current);
                            current = fileLength;
                        }
                        contents = new byte[size];
                        fis.read(contents, 0, size);

                        sendToServerSound("ShortRecordSave", current, fileLength, contents);
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "an error occurred when trying to send the sound", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: ", e);
                }
            } else {
                sendToServerSelection((String) soundSelector.getSelectedItem());
            }
            startActivity(new Intent(ShortRecord.this, LongRecord.class));
        });
        btnSave = findViewById(R.id.buttonSave);

        btnSave.setOnClickListener(v -> saveSound(audioSaveFile));

    }

    public void switchOnRecorder() {
        btnRecord.setSelected(true);
    } // change button mode

    public void switchOffRecorder() {
        btnRecord.setSelected(false);
    } // change button mode

    public void switchOnPlayer() {
        btnPlay.setSelected(true);
    } // change button mode

    public void switchOffPlayer() {
        btnPlay.setSelected(false);
    } // change button mode

    private static void sendToServerSound(String code, long current, long fileLength, byte[] contents) {// send to server the sound
        byte[] msg = (code + "~" + MainActivity.getUsername() + "~" + (current == fileLength ? 1 : 0) + "~").getBytes();
        byte[] toSend = new byte[msg.length + contents.length + 1];
        System.arraycopy(msg, 0, toSend, 1, msg.length);
        System.arraycopy(contents, 0, toSend, msg.length + 1, contents.length);
        toSend[0] = (byte) msg.length;
        SendRecv.send(MainActivity.getmHandler(), MainActivity.getIp(), toSend);
        SendRecv.receiveData();
    }

    private static void sendToServerSelection(String soundName) { //send to the server the sound name the user chose to set as the short sound
        byte[] msg = ("ShortRecordExist" + "~" + MainActivity.getUsername() + "~" + soundName + "~").getBytes();
        byte[] toSend = new byte[msg.length + +1];
        System.arraycopy(msg, 0, toSend, 1, msg.length);
        toSend[0] = (byte) msg.length;
        SendRecv.send(MainActivity.getmHandler(), MainActivity.getIp(), toSend);
        SendRecv.receiveData();
    }

    private static String getSounds() {// returns all the use's sound and input into the spinner
        byte[] msg = ("GetSoundsNames" + "~" + MainActivity.getUsername() + "~").getBytes();
        byte[] toSend = new byte[msg.length + 1];
        System.arraycopy(msg, 0, toSend, 1, msg.length);
        toSend[0] = (byte) msg.length;
        SendRecv.send(MainActivity.getmHandler(), MainActivity.getIp(), toSend);
        return SendRecv.receiveData();
    }

    private void saveSound(File audioSaveFile) {
        final EditText input = new EditText(this);
        input.setHint("Enter a name"); // Set the hint for the EditText

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Recording");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = input.getText().toString().trim(); // Trim leading and trailing spaces
            if (!name.isEmpty()) {
                Scanner scanner = new Scanner(name);
                String validationResult = scanner.findInLine("[^0-9a-zA-Z]+");
                if (validationResult != null) {
                    // Invalid character found.
                    Toast.makeText(this, "FIle name can only contain numbers and letters", Toast.LENGTH_SHORT).show();
                } else {
                    // Save recording logic
                    if (!savedRecordingNames.contains(name)) {
                        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(audioSaveFile))) {
                            //Send to server
                            long current = 0;
                            long fileLength = audioSaveFile.length();
                            byte[] contents;
                            while (current != fileLength) {
                                int size = 10000;
                                if (fileLength - current >= size)
                                    current += size;
                                else {
                                    size = (int) (fileLength - current);
                                    current = fileLength;
                                }
                                contents = new byte[size];
                                fis.read(contents, 0, size);

                                sendToServerSound("SaveRecord" + "~" + name, current, fileLength, contents);
                            }

                            // save name in client
                            savedRecordingNames.add(name); // Add name to the list
                            spinnerAdapter.notifyDataSetChanged(); // Update the spinner data
                            Toast.makeText(ShortRecord.this, "Recording saved as " + name, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error saving recording or updating spinner", e);
                            Toast.makeText(ShortRecord.this, "An error occurred while saving. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ShortRecord.this, "This name is already in use. Please choose a different name.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(ShortRecord.this, "Please enter a name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    boolean checkPermissions() {
        int recordAudioPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        int readMediaAudioPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_AUDIO);

        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED || readMediaAudioPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ShortRecord.this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO}, 1);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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