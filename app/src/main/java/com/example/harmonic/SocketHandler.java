package com.example.harmonic;

import android.os.Handler;

import java.net.Socket;

public class SocketHandler {
    private static Socket socket;
    private static Handler Hreceiver;

    public static synchronized Socket getSocket() {
        return socket;
    }

    public static synchronized void setSocket(Socket socket) {
        SocketHandler.socket = socket;
    }

    public static synchronized Handler getHreceiver() {
        return Hreceiver;
    }

    public static synchronized void setHreceiver(Handler Hreceiver) {
        SocketHandler.Hreceiver = Hreceiver;

    }
}
