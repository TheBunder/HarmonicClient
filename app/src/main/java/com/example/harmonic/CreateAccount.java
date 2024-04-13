package com.example.harmonic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateAccount extends AppCompatActivity {
    EditText username, password, repassword;
    Button btnBack, btnCreate;
    DBHelper DB;

    private static final int MINIMUM_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // DB configuration
        DB = new DBHelper(this);

        // Text configuration
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        repassword = (EditText) findViewById(R.id.repassword);

        // Button configuration
        btnBack = findViewById(R.id.buttonLeft); // back to log in
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CreateAccount.this, MainActivity.class));
            }
        });


        btnCreate = findViewById(R.id.buttonCreat);// sign up
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString();
                String pass = password.getText().toString();
                String repass = repassword.getText().toString();

                byte[] user_and_passB = ("SignUp~"+user+"~"+pass).getBytes();
                byte[] to_send = new byte[user_and_passB.length+1];
                System.arraycopy(user_and_passB,0,to_send,1,user_and_passB.length);
                to_send[0]= (byte) user_and_passB.length;

                if (user.equals("") || pass.equals("") || repass.equals("")) {
                    Toast.makeText(CreateAccount.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (pass.equals(repass)) {
                        if (pass.length() >= MINIMUM_PASSWORD_LENGTH) { // Check password length
                            SendRecv.send(MainActivity.getmHandler(), MainActivity.getIp(), to_send);
                            String answer = SendRecv.receive_data();
                            if (!answer.equals("Username is in use")) {
                                if (answer.equals("Sign up successful")) {
                                    Toast.makeText(CreateAccount.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(CreateAccount.this, MainActivity.class));
                                } else {
                                    Toast.makeText(CreateAccount.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(CreateAccount.this, "User already exists! Please sign in", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CreateAccount.this, "Password must be at least " + MINIMUM_PASSWORD_LENGTH + " characters long", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CreateAccount.this, "Password not matching", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }
}
