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

public class db_UserDiscovery extends AppCompatActivity {
    Blob myBlob, friendBlob;
    byte[] imageInByte;
    String myUsername, friendUsername, friendUUID, myUUID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        myUUID = intent.getStringExtra("UUID");
        /*
        imageInByte = intent.getByteArrayExtra("ProfileImage");
        myBlob = new Blob("image/*", imageInByte);
        myUsername = intent.getStringExtra("Username");
        */

        //OnClick any user
        try {
            // Get the database (and create it if it doesn’t exist).
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            Database friendDatabase = new Database("friendList", config);

            // Create a new document (i.e. a record) in the database.
            MutableDocument friendDoc = new MutableDocument();

            //imply the owner of friends by UUID, maybe other info
            friendDoc.setString("myUUID", myUUID);
            /*
            friendDoc.setBlob("myImage", myBlob);
            friendDoc.setString("myUsername", myUsername);
            */

            /*
            * a function to get friends' information and save document
            *
            * friendUUID = getString(otherDevice);
            * imageInByte = getBlob(otherDevice);
            * friendBlob = = new Blob("image/*", imageInByte);
            * friendUsername = getString(otherDevice);
            *
            * friendDoc.setString("friendUUID", friendUUID);
            * friendDoc.setBlob("friendBlob", friendBlob);
            * friendDoc.setString("friendUsername", friendUsername);
            *
            * friendDatabase.save(friendDoc);
            *
            * //start an intent to new activity
            * Intent intent1 = new Intent(UserDiscovery.this, ChatActivity.class);
            * intent.putExtra("FriendUUID", friendUUID);
            * startActivity(intent1);
            * finish();
            *
            */

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }
}
