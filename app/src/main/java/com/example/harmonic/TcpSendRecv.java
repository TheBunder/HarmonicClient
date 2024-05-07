package com.example.harmonic;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

public class TcpSendRecv implements Runnable {
    private String ip = MainActivity.getIp();
    final static int LEN_SIZE = 9;
    byte[] bdata;

    public TcpSendRecv(Handler mHandler, String ip, byte[] bdata) {
        if (ip.isEmpty())
            this.ip = ip;
        this.bdata = bdata;
        Handler h = SocketHandler.getHreceiver();
        if (h != mHandler) {
            SocketHandler.setHreceiver(mHandler);
        }
    }


    @Override
    public synchronized void run() {
        String TAG = "%%!%%";
        Socket sk;

        sk = SocketHandler.getSocket();
        Log.d("sock", String.valueOf(sk));
        if (sk == null) {
            try {
                Log.d(TAG, "Before Connect");
                sk = new Socket(ip, 2525);
                SocketHandler.setSocket(sk);

                Log.d(TAG, "connected");

            } catch (UnknownHostException e) {
                Log.e(TAG, "ERROR UnknownHostException socket");

            } catch (IOException e) {
                Log.e(TAG, "ERROR IOException socket");
            } catch (Exception e) {
                Log.e(TAG, String.valueOf(e));
            }
        }

// Create the header string with the length of the data and a separato
        String s = String.format(Locale.US, "%09d", bdata.length) + "|";

        byte[] header = s.getBytes();
        Log.d(TAG, "Before Send 1");
        try {
            assert sk != null;
            DataOutputStream dout = new DataOutputStream(sk.getOutputStream());
            Log.d(TAG, "Before Send 2");
            byte[] c = new byte[header.length + bdata.length];
            System.arraycopy(header, 0, c, 0, header.length);
            System.arraycopy(bdata, 0, c, header.length, bdata.length);


            Log.d(TAG, "Before Send 2");
            dout.write(c);

            dout.flush();
            Log.d(TAG, "Msg sent");
            //pw.close();

        } catch (IOException e) {
            Log.e(TAG, "ERROR write " + e.getMessage());

            Handler mHandler = SocketHandler.getHreceiver();
            Message msg = mHandler.obtainMessage();
            msg.obj = "Socket Error";
            mHandler.sendMessage(msg);

        }

    }

    static class Listener implements Runnable {
        private BufferedInputStream bis;
        private DataInputStream dis;
        private static String gotData;
        private static final String TAG = "Listener";

        public Listener(Socket sk) {


            try {
                bis = new BufferedInputStream(sk.getInputStream());
                dis = new DataInputStream(bis);
            } catch (IOException e) {
                Log.e(TAG, "ERROR buffer read " + e.getMessage());
                e.printStackTrace();
            }


        }

        public static String getData() {
            return gotData;
        }

        private static void setData(String data) {
            gotData = data;
        }

        @Override
        public void run() {

            boolean ok = true;

            while (ok) {
                try {
                    byte[] lengthBytes = new byte[LEN_SIZE + 1];
                    dis.readFully(lengthBytes);
                    String headerSize = new String(lengthBytes).substring(0, lengthBytes.length - 1);
                    int length = Integer.parseInt(headerSize);

                    byte[] data = new byte[length];
                    int totalBytesRead = 0;
                    while (totalBytesRead < length) {
                        int bytesRead = dis.read(data, totalBytesRead, length - totalBytesRead);
                        if (bytesRead == -1) {
                            throw new IOException("Socket closed before reading complete data");
                        }
                        totalBytesRead += bytesRead;
                    }

                    String dataStr = new String(data, "UTF-8");
                    Log.d(TAG, " ** got data :" + dataStr);
                    // Set the received data variable
                    setData(dataStr);
                    Handler mHandler = SocketHandler.getHreceiver();
                    if (mHandler == null) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler = SocketHandler.getHreceiver();
                    }
                    if (mHandler == null) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler = SocketHandler.getHreceiver();
                    }

                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage();
                        msg.obj = data;
                        mHandler.sendMessage(msg);
                    } else {
                        Log.e(TAG, "Handle = Null skipping msg=" + data.toString());
                    }

                    // Set ok to false to finish the thread
                    ok = false;
                } catch (IOException e) {
                    Log.e(TAG, "ERROR read line- " + e.getMessage() + ".");
                    e.printStackTrace();
                    ok = false;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "ERROR InterruptedException " + e.getMessage());
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Login Listener finished ");
        }
    }

}