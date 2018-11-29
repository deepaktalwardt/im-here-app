package com.example.user.teamproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ListView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.google.gson.JsonIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OldMessageActivity extends AppCompatActivity {


    Database friendDatabase, chatRoomDatabase;
    // friend info
    String friendUUID, friendUsername, docID;

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

        Intent intent = getIntent();
        friendUUID = intent.getStringExtra("FriendUUID");
        friendUsername = intent.getStringExtra("FriendUsername");

        getSupportActionBar().setTitle(friendUsername);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
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
//        int size = rs.allResults().size();
        for (Result result: rs) {
            Log.d("OldMessageActivity", result.getString("message") + result.getString("chatType"));
            try {
                if (result.getString("chatType").equals("send")) {
                    String msg = result.getString("message");
                    JSONObject jsonMsg = new JSONObject(msg);
                    String chatMessage = jsonMsg.getString("message");

                    ChatModel model = new ChatModel(chatMessage, true);
                    adapter = new CustomAdapter(getApplicationContext(), msgList);
                    oldMessageList.setAdapter(adapter);
                    msgList.add(model);
                    adapter.notifyDataSetChanged();
                }

                else if (result.getString("chatType").equals("receive")) {
                    String msg = result.getString("message");
                    JSONObject jsonMsg = new JSONObject(msg);
                    String chatMessage = jsonMsg.getString("message");

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

//        //get the data and append to a list
//        ArrayList<String> listData = new ArrayList<>();
//        for (int i = 0; i < size; i++) {
//            rs = query.execute();
//            String chatType = rs.allResults().get(i).getString("chatType");
//
//            if(chatType.equals("send")){
//                String sentMsg = rs.allResults().get(i).getString("message");
//                Toast toast = Toast.makeText(OldMessageActivity.this, "chatType: " + chatType, Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
//                toast.show();
//
//                try {
//                    JSONObject sentMessage = new JSONObject(sentMsg);
//                    String message = sentMessage.getString("message");
//
//                    ChatModel model = new ChatModel(message, true);
//                    adapter = new CustomAdapter(getApplicationContext(), msgList);
//                    messageList.setAdapter(adapter);
//                    msgList.add(model);
//                    adapter.notifyDataSetChanged();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else if(chatType.equals("receive")){
//                JSONObject parsedMessage = (JSONObject) rs.allResults().get(i).getValue("message");
//                try {
//                    String message = parsedMessage.getString("message");
//
//                    ChatModel model = new ChatModel(message, false);
//                    adapter = new CustomAdapter(getApplicationContext(), msgList);
//                    messageList.setAdapter(adapter);
//                    msgList.add(model);
//                    adapter.notifyDataSetChanged();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
