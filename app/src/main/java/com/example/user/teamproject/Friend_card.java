package com.example.user.teamproject;

import android.widget.ImageView;

public class Friend_card {
    private String mUsername;
    private String mUUID;
    private String mTime;
    private int mConnection;

    public Friend_card(String username, String UUID, String time, int connection) {
        mUsername = username;
        mUUID = UUID;
        mTime = time;
        mConnection = connection;
    }

    public String getUsername(){
        return mUsername;
    }

    public String getUUID(){
        return mUUID;
    }

    public String getTime(){
        return mTime;
    }

    public int getConnection(){
        return mConnection;
    }

}
