package com.example.user.teamproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OldMessageActivity extends AppCompatActivity {
    // Databases
    DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
    Database friendDatabase, chatRoomDatabase;
    // friend info
    String friendUUID, friendUsername, docID;

    // Chat Objects
    ArrayList msgList = new ArrayList<ChatModel>();
    CustomAdapter adapter;
    ListView messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_message);

        Intent intent = getIntent();
        friendUUID = intent.getStringExtra("FriendUUID");
        friendUsername = intent.getStringExtra("friendUsername");
        getSupportActionBar().setTitle(friendUsername);
        try {
            chatRoomDatabase = new Database(friendUUID, config);
            loadMessage(chatRoomDatabase);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }


    private void loadMessage(Database chat) throws CouchbaseLiteException {
        Query query = QueryBuilder.select(SelectResult.property("index"))
                .from(DataSource.database(chat))
                .where(Expression.property("index"));
        com.couchbase.lite.ResultSet rs = query.execute();
        int size = rs.allResults().size();

        //get the data and append to a list
        ArrayList<String> listData = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            query = QueryBuilder.select(SelectResult.property("chatType"), SelectResult.property("message"))
                    .from(DataSource.database(chatRoomDatabase))
                    .where(Expression.property("index").equalTo(Expression.property("index").value(i)));
            rs = query.execute();
            Boolean chatType = rs.allResults().get(0).getBoolean("chatType");
            if(chatType.equals("send")){
                JSONObject sentMessage = (JSONObject) rs.allResults().get(0).getValue("message");
                try {
                    String message = sentMessage.getString("message");

                    ChatModel model = new ChatModel(message, true);
                    adapter = new CustomAdapter(getApplicationContext(), msgList);
                    messageList.setAdapter(adapter);
                    msgList.add(model);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if(chatType.equals("receive")){
                JSONObject parsedMessage = (JSONObject) rs.allResults().get(0).getValue("message");
                try {
                    String message = parsedMessage.getString("message");

                    ChatModel model = new ChatModel(message, false);
                    adapter = new CustomAdapter(getApplicationContext(), msgList);
                    messageList.setAdapter(adapter);
                    msgList.add(model);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
