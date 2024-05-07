package com.example.harmonic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LongRecord extends AppCompatActivity {

    private ImageButton btnRecord;

    TextView occurrences;


    private static final String TAG = "LongRecord";

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_record);

        occurrences = findViewById(R.id.Occurrences);

        btnRecord = findViewById(R.id.buttonRecord);
        RecordingRunnable recordingRunnable = new RecordingRunnable(occurrences);
        btnRecord.setOnClickListener(v -> {
            try {
                if (!btnRecord.isSelected()) {// check whether to stop or to start

                    if (checkPermissions()) {// check if there are needed premision
                        Log.v(TAG, "Have permission");
                        //change from idol to record (start record)
                        switchOn();
                        // start recording
                        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
                        Thread recordingThread = new Thread(recordingRunnable, "Recording Thread");
                        recordingThread.start();
                        //Log.v(TAG, "heyy!!! did you get here?!");

                    } else {
                        Log.v(TAG, "missing permission");
                        ActivityCompat.requestPermissions(LongRecord.this,
                                new String[]{android.Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO}, 1);
                    }

                } else {// ask for premision
                    recordingRunnable.stop();
                    switchOff();
                    Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Log.e(TAG, "error recording", e);  // Log the error for debugging
            }
        });

    }

    public void switchOn() {
        btnRecord.setSelected(true);
    } // change button mode

    public void switchOff() {
        btnRecord.setSelected(false);
    } // change button mode


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    boolean checkPermissions() {
        int recordAudioPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        int readMediaAudioPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_AUDIO);

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
