package com.example.user.teamproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

public class InitiateActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiate);

        Handler handler = new Handler();
        try {
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            Database userDatabase = new Database("userList", config);

            Query query = QueryBuilder
                    .select(SelectResult.property("userDocId"))
                    .from(DataSource.database(userDatabase))
                    .where(Expression.property("hasLogin").equalTo(Expression.string("true")));
            ResultSet rs = query.execute();
            int size = rs.allResults().size();
            if( size == 1){
                rs = query.execute();
                String userDocId = rs.allResults().get(0).getString("userDocId");
                Document userDoc = userDatabase.getDocument(userDocId);

                //load login user's information
                byte[] imageInByte = userDoc.getBlob("image").getContent();
                String username = userDoc.getString("username");
                String UUID = userDoc.getString("UUID");

                final Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("UserDocId", userDocId);
                intent.putExtra("ProfileImage", imageInByte);
                intent.putExtra("UUID", UUID);
                intent.putExtra("Username", username);
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }else if( size > 1){
                for(int i = 0; i < size; i++){
                    rs = query.execute();
                    String userDocId = rs.allResults().get(i).getString("userDocId");
                    MutableDocument userDoc = userDatabase.getDocument(userDocId).toMutable();

                    userDoc.setString("hasLogin", "false");
                    userDatabase.save(userDoc);
                }
                final Intent intent = new Intent(this, LoginPageActivity.class);
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }else{
                final Intent intent = new Intent(this, LoginPageActivity.class);
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }
}
