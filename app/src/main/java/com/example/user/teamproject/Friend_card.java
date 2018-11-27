package com.example.user.teamproject;

public class Friend_card {
    private String mUsername;
    private String mUUID;

    public Friend_card(String username, String UUID) {
        mUsername = username;
        mUUID = UUID;
    }

    public String getUsername(){
        return mUsername;
    }

    public String getUUID(){
        return mUUID;
    }

    public void changeText(String text){
        mUsername = text;
    }
}
