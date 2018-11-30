package com.example.user.teamproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.SelectResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OldMessageActivity extends AppCompatActivity {
    Database chatRoomDatabase;
    // friend info
    String friendUUID, friendUsername;

    // Chat Objects
    ArrayList<ChatModel> msgList = new ArrayList<ChatModel>();
    CustomAdapter adapter;
    ListView oldMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Databases config
        DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_message);
        oldMessageList = (ListView) findViewById(R.id.oldMessageList);

        //get friend info from HomeActivity which list on cards
        Intent intent = getIntent();
        friendUUID = intent.getStringExtra("FriendUUID");
        friendUsername = intent.getStringExtra("FriendUsername");

        //show title on actionbar
        getSupportActionBar().setTitle(friendUsername);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            //populate message
            chatRoomDatabase = new Database(friendUUID, config);
            loadMessage(chatRoomDatabase);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }


    private void loadMessage(Database chat) throws CouchbaseLiteException {
        Query query = QueryBuilder.select(SelectResult.property("index"), SelectResult.property("chatType"), SelectResult.property("message"))
                .from(DataSource.database(chat));
        com.couchbase.lite.ResultSet rs = query.execute();
        for (Result result : rs) {
            Log.d("OldMessageActivity", result.getString("message") + result.getString("chatType"));
            //check if the message is send out by self or receive by friend
            try {
                if (result.getString("chatType").equals("send")) {
                    //get message from database
                    String msg = result.getString("message");
                    JSONObject jsonMsg = new JSONObject(msg);
                    String chatMessage = jsonMsg.getString("message");

                    //sort message to right
                    ChatModel model = new ChatModel(chatMessage, true);
                    adapter = new CustomAdapter(getApplicationContext(), msgList);
                    oldMessageList.setAdapter(adapter);
                    msgList.add(model);
                    adapter.notifyDataSetChanged();
                } else if (result.getString("chatType").equals("receive")) {
                    //get message from database
                    String msg = result.getString("message");
                    JSONObject jsonMsg = new JSONObject(msg);
                    String chatMessage = jsonMsg.getString("message");

                    //sort message to left
                    ChatModel model = new ChatModel(chatMessage, false);
                    adapter = new CustomAdapter(getApplicationContext(), msgList);
                    oldMessageList.setAdapter(adapter);
                    msgList.add(model);
                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
