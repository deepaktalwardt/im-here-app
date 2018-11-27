package com.example.user.teamproject;

public class Friend_card {
    private String mUsername;
    private String mUUID;
    private String mTime;

    public Friend_card(String username, String UUID, String time) {
        mUsername = username;
        mUUID = UUID;
        mTime = time;
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

}
