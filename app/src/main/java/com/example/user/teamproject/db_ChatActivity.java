package com.example.user.teamproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.couchbase.lite.Array;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import java.util.ArrayList;
import java.util.Calendar;

public class db_ChatActivity extends AppCompatActivity {
    String friendUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Textfield = findViewByID();

        Intent intent = getIntent();
        friendUUID = intent.getStringExtra("FriendUUID");

        try {
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            Database friendChat = new Database(friendUUID, config);

//            sendBtn.setOnclickListener() {
//                MutableDocument message = new MutableDocument();
//                int lastIndex = (int) friendChat.getCount();
//                message.setInt("index", lastIndex + 1);
//                message.setString("sender", username);
//                message.setString("context", Textfield.getText());
//                message.setString("time", String.valueOf(Calendar.getInstance().getTime()));
//                friendChat.save(message);
//            }
//
//            Query query = QueryBuilder.select(SelectResult.property("index"))
//                    .from(DataSource.database(friendChat))
//                    .where(Expression.property("index"));
//            ResultSet rs = query.execute();
//            int size = rs.allResults().size();
//            messageWall(friendChat);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void messageWall(Database friendChat) throws CouchbaseLiteException {
        Query query = QueryBuilder.select(SelectResult.property("index"))
                .from(DataSource.database(friendChat))
                .where(Expression.property("index"));
        ResultSet rs = query.execute();
        int size = rs.allResults().size();

        //get the data and append to a list
        ArrayList<String> listData = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            query = QueryBuilder.select(SelectResult.property("sender"))
                    .from(DataSource.database(friendChat))
                    .where(Expression.property("index").equalTo(Expression.value(i)));
            rs = query.execute();
            String sender = rs.allResults().get(0).getString("sender");

            query = QueryBuilder.select(SelectResult.property("context"))
                    .from(DataSource.database(friendChat))
                    .where(Expression.property("index").equalTo(Expression.value(i)));
            rs = query.execute();
            String context = rs.allResults().get(0).getString("context");

            query = QueryBuilder.select(SelectResult.property("time"))
                    .from(DataSource.database(friendChat))
                    .where(Expression.property("index").equalTo(Expression.value(i)));
            rs = query.execute();
            String time = rs.allResults().get(0).getString("time");
        }
/*
        //get the value from the data in column, then add it to the ArrayList
        listData.add(intent.getStringExtra("Username"));
        listData.add(intent.getStringExtra("Name"));
        listData.add(intent.getStringExtra("Extension"));

        //create the list adapter and set the adapter
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        users.setAdapter(adapter);
        */
    }

}
