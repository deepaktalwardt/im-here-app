package com.example.user.teamproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.couchbase.lite.Blob;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.MutableDocument;

public class SearchList extends AppCompatActivity {
    Blob myBlob;
    byte[] imageInByte;
    String myUsername, myName, myDeviceId, referChatRoom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        myDeviceId = intent.getStringExtra("DeviceId");
        /*
        imageInByte = intent.getByteArrayExtra("ProfileImage");
        myBlob = new Blob("image/*", imageInByte);
        myUsername = intent.getStringExtra("Username");
        myName = intent.getStringExtra("Name");
        */

        //OnClick any user
        try {
            // Get the database (and create it if it doesn’t exist).
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            Database friendDatabase = new Database("friendList", config);

            // Create a new document (i.e. a record) in the database.
            MutableDocument friendDoc = new MutableDocument();

            //imply the owner of friends by extension, maybe other info
            friendDoc.setString("myDeviceId", myDeviceId);
            /*
            friendDoc.setBlob("myImage", myBlob);
            friendDoc.setString("myUsername", myUsername);
            friendDoc.setString("myName", myName);
            */

            /*
            * a function to get friends' information and save document
            *
            * String friendDeviceId = getString(otherDevice);
            * Blob friendImage = getBlob(otherDevice);
            * String friendUsername = getString(otherDevice);
            * String friendName = getString(otherDevice);
            *
            *
            * friendDoc.setString("friendDeviceId", friendDeviceId);
            * friendDoc.setBlob("friendImage", friendImage);
                friendDoc.setString("friendUsername", friendUsername);
                friendDoc.setString("friendName", friendName);
            *
            * friendDatabase.save(friendDoc);
            *
            * //start an intent to new activity
            * Intent intent1 = new Intent(SearchList.this, ChatRoom.class);
            * intent.putExtra("MyDeviceId", myDeviceId);
            * intent.putExtra("FriendDeviceId", friendDeviceId);
            * startActivity(intent1);
            * finish();
            *
            */

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }
}
