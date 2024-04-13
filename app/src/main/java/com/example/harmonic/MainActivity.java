package com.example.harmonic;

import androidx.appcompat.app.AppCompatActivity;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    EditText username, password;
    Button btnLogin, btnSignup, btnSend;
    DBHelper DB;
    static String user;
    private static Handler mHandler;

    private static String ip = "192.168.31.13";

    public static Handler getmHandler() {
        return mHandler;
    }
    public static String getIp() {
        return ip;
    }
    public static String getUsername(){
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
            //startActivity(new Intent(getApplicationContext(), ShortRecord.class));

            user = username.getText().toString();
            String pass = password.getText().toString();
            byte[] user_and_passB = ("Login~"+user+"~"+pass).getBytes();
            byte[] to_send = new byte[user_and_passB.length+1];
            System.arraycopy(user_and_passB,0,to_send,1,user_and_passB.length);
            to_send[0]= (byte) user_and_passB.length;

            if (user.equals("") || pass.equals("")) {
                Toast.makeText(MainActivity.this, "Please enter all the fields as mentioned", Toast.LENGTH_SHORT).show();
            } else {
                SendRecv.send(mHandler, ip, to_send);
                String answer = SendRecv.receive_data();
                if (answer.equals("Username and password match")) {
                    Toast.makeText(MainActivity.this, "Sign in successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), ShortRecord.class));
                } else {
                    Toast.makeText(MainActivity.this, "Username or password is invalid", Toast.LENGTH_SHORT).show();
                }
            }

        });

        btnSignup = findViewById(R.id.buttonSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateAccount.class)); // move to sign in
            }
        });

        btnSend = findViewById(R.id.send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] ba = "hello world".getBytes();

                SendRecv.send(mHandler, ip, ba);
                String data = SendRecv.receive_data();
                Log.d("recv", data);

            }
        });

    }

    public static void SetEncryption(Handler mHandler) {


        try {
            byte[] arr = "bralala".getBytes();
            SendRecv.send(mHandler, ip, arr);

            String DPH_srv_key = SendRecv.receive_data();
            Log.d("recv", DPH_srv_key);

            String[] parts = DPH_srv_key.split("\\|");
            BigInteger srv_public_key = new BigInteger(parts[0]);
            BigInteger g = new BigInteger(parts[1]);
            BigInteger p = new BigInteger(parts[2]);
            //Log.d("EncryptionUtilLen","p:" + p.toString().length());
            Random random = new Random();
            BigInteger clientPrivateKey = new BigInteger(2048, random);
            BigInteger clientPublicKey = g.modPow(clientPrivateKey, p);

            SendRecv.send(mHandler, ip, clientPublicKey.toByteArray());
            BigInteger SharedKey = srv_public_key.modPow(clientPrivateKey, p);
            Log.d("DPH", String.valueOf(SharedKey));

            // Hash the random value with SHA-256 and take the first 16 bytes of the digest
            //byte[] secretKey = Arrays.copyOfRange(MessageDigest.getInstance("SHA-256").digest((SharedKey.toByteArray())), 0, 16);
            MessageDigest md = MessageDigest.getInstance("SHA256");
            byte[] hash_key = md.digest((SharedKey.toString()).getBytes());
            byte[] secretKey = Arrays.copyOf(hash_key, 16);
            Log.d("AES", Arrays.toString(secretKey));


            //Socket sk = SocketHandler.getSocket();
            //String iv_srv = sk.getInputStream().toString();
            SecretKeySpec aes_key = new SecretKeySpec(secretKey, "AES");
            Cipher cipher_encryption = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher_encryption.init(Cipher.ENCRYPT_MODE, aes_key);

            // Send IV
            SendRecv.send(mHandler, ip, cipher_encryption.getIV());

            IvParameterSpec ivParams = new IvParameterSpec(cipher_encryption.getIV());
            Cipher cipher_decryption = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher_decryption.init(Cipher.DECRYPT_MODE, aes_key, ivParams);

            //byte[] result = new byte[encryptedPassword.length + iv.length];
            //System.arraycopy(encryptedPassword, 0, result, 0, encryptedPassword.length);

            //IvParameterSpec ivParams1 = new IvParameterSpec(cipher_encryption.getIV());

            //send(encryptedPassword);
            SendRecv.setAes_key(aes_key);
            SendRecv.setIv(ivParams);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

}

