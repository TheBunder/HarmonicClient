package com.example.harmonic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText username, password;
    Button btnLogin, btnSignup, btnSend;
    DBHelper DB;
    static String user;
    private static Handler mHandler;

    private static final String ip = "192.168.31.13";

    private static final String TAG = "Login";

    public static Handler getmHandler() {
        return mHandler;
    }

    public static String getIp() {
        return ip;
    }

    public static String getUsername() {
        return user;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // handler configuration
        mHandler = new Handler(Looper.getMainLooper()) {
        };

        // DB configuration
        DB = new DBHelper(this);

        // Text configuration
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);


        // Button configuration

        btnLogin = findViewById(R.id.buttonLogin); // log in
        btnLogin.setOnClickListener(v -> {

            user = username.getText().toString();
            String pass = password.getText().toString();
            byte[] user_and_passB = ("Login~" + user + "~" + pass).getBytes();
            byte[] to_send = new byte[user_and_passB.length + 1];
            System.arraycopy(user_and_passB, 0, to_send, 1, user_and_passB.length);
            to_send[0] = (byte) user_and_passB.length;

            if (user.equals("") || pass.equals("")) {
                Toast.makeText(MainActivity.this, "Please enter all the fields as mentioned", Toast.LENGTH_SHORT).show();
            } else {
                SendRecv.setEncryption(mHandler);
                try {
                    SendRecv.send_encrypted(to_send);
                } catch (Exception e) {
                    Log.e(TAG, "Error: ", e);
                }
                String answer = SendRecv.receiveData();
                if (answer.equals("Username and password match")) {
                    Toast.makeText(MainActivity.this, "Sign in successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), ShortRecord.class));
                } else {
                    Toast.makeText(MainActivity.this, "Username or password is invalid", Toast.LENGTH_SHORT).show();
                }
            }

        });

        btnSignup = findViewById(R.id.buttonSignup);
        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CreateAccount.class)) // move to sign in
        );
    }

}

