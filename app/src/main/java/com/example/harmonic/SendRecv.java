package com.example.harmonic;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.NoSuchAlgorithmException;

public class SendRecv {
    private static Handler MHandler;
    private static SecretKeySpec Aes_key;
    private static IvParameterSpec ivParams;




    public static synchronized void setMHandler(Handler MHandler) {
        SendRecv.MHandler = MHandler;
    }

    /*public static synchronized Handler getMHandler() {
        return SendRecv.MHandler;
    }*/

    public static synchronized void setAes_key(SecretKeySpec Aes_key) {
        SendRecv.Aes_key = Aes_key;
    }

    public static synchronized SecretKeySpec getAes_key() {
        return SendRecv.Aes_key;
    }

    public static synchronized void setIv(IvParameterSpec iv) {
        SendRecv.ivParams = iv;
    }

    public static synchronized IvParameterSpec getIv() {
        return SendRecv.ivParams;
    }

    public static void send_encrypted(byte[] ba) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher encryption_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        encryption_cipher.init(Cipher.ENCRYPT_MODE, Aes_key, ivParams);
        ba = encryption_cipher.doFinal(padToMultipleOf16(ba));
        ba = Base64.getEncoder().encode(ba);
        Log.d("send_encrypted", new String(ba));
        Thread sender = new Thread(new TcpSendRecv(MHandler, SessionData.getServerIp(), ba));
        sender.start();
        try {
            // Wait for the sender thread to finish
            sender.join();
        } catch (InterruptedException e) {
            // Handle the InterruptedException if needed
            e.printStackTrace();
        }

    }

    public static void send(Handler mHandler, String ip , byte[] ba){
        Thread sender = new Thread(new TcpSendRecv(mHandler, ip, ba));
        sender.start();
        try {
            // Wait for the sender thread to finish
            sender.join();
        } catch (InterruptedException e) {
            // Handle the InterruptedException if needed
            e.printStackTrace();
        }
    }

    public static String receive_data()
    {
        Socket sk = SocketHandler.getSocket();
        Thread listener = new Thread(new TcpSendRecv.Listener(sk));
        listener.start();
        try {
            listener.join();
            return TcpSendRecv.Listener.getData();
        } catch (InterruptedException e) {
            // Handle the InterruptedException, if needed
            e.printStackTrace();
        }
        return "";
    }

    public static byte[] padToMultipleOf16(byte[] input) {
        int currentLength = input.length;
        int paddingNeeded = (16 - (currentLength % 16)) % 16; // Calculate padding needed

        // Create a new array with the required length
        byte[] paddedBytes = Arrays.copyOf(input, currentLength + paddingNeeded);

        // Fill the padding area with zeros (you can change this if a different padding byte is needed)
        for (int i = currentLength; i < paddedBytes.length; i++) {
            paddedBytes[i] = " ".getBytes()[0]; // Use any byte value you prefer for padding
        }

        return paddedBytes;
    }

    public static String receive_decrypted() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        Socket sk = SocketHandler.getSocket();
        Thread listener = new Thread(new TcpSendRecv.Listener(sk));
        listener.start();
        Cipher decryption_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        decryption_cipher.init(Cipher.DECRYPT_MODE, Aes_key, ivParams);
        try {
            listener.join();

            String data = TcpSendRecv.Listener.getData();
            Log.d("recv", data);
            byte[] bdata = Base64.getDecoder().decode(data);
            String original_data = new String(decryption_cipher.doFinal(bdata), StandardCharsets.UTF_8);
            Log.d("receive_decrypted", original_data);
            return original_data;

        } catch (InterruptedException e) {
            // Handle the InterruptedException, if needed
            e.printStackTrace();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return "";
    }
}
