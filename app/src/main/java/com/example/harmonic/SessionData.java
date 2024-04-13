package com.example.harmonic;

import android.location.Location;

public class SessionData {
    private static String username;
    private static String ServerIP = MainActivity.getIp();
    private static String instagram;
    private static String phone_number;
    private static String info;
    private static String birth_date;
    private static String share_location_with;
    private static String receive_from;
    private static Location location;

    public static synchronized String getUsername() {
        return username;
    }

    public static synchronized void setUsername(String username) {
        SessionData.username = username;
    }

    public static synchronized void setServerIp(String ServerIp) {
        SessionData.ServerIP = ServerIp;
    }

    public static synchronized String getServerIp() {
        return SessionData.ServerIP;
    }

    public static synchronized void setInstagram(String instagram) {
        SessionData.instagram = instagram;
    }

    public static synchronized String getInstagram() {
        return SessionData.instagram;
    }

    public static synchronized void setPhone_number(String phone_number) {
        SessionData.phone_number = phone_number;
    }

    public static synchronized String getPhone_number() {
        return SessionData.phone_number;
    }

    public static synchronized void setInfo(String info) {
        SessionData.info = info;
    }

    public static synchronized String getInfo() {
        return SessionData.info;
    }

    public static synchronized void setBirth_date(String birth_date) {
        SessionData.birth_date = birth_date;
    }

    public static synchronized String getBirth_date() {
        return SessionData.birth_date;
    }

    public static synchronized void setShare_location_with(String share_location_with) {
        SessionData.share_location_with = share_location_with;
    }

    public static synchronized String getShare_location_with() {
        return SessionData.share_location_with;
    }

    public static synchronized void setReceive_from(String receive_from) {
        SessionData.receive_from = receive_from;
    }

    public static synchronized String getReceive_from() {
        return SessionData.receive_from;
    }

    public static synchronized Location getLocation() {
        return location;
    }

    public static synchronized void setLocation(Location location) {
        SessionData.location = location;
    }
}
