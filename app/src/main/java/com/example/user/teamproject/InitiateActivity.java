package com.example.user.teamproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;

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
    // TXT RECORD properties
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_imhereapp";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public String myUUID;
    public String myUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiate);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Handler handler = new Handler();
        try {
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            Database userDatabase = new Database("userList", config);

            //check if any user has login
            Query query = QueryBuilder
                    .select(SelectResult.property("userDocId"))
                    .from(DataSource.database(userDatabase))
                    .where(Expression.property("hasLogin").equalTo(Expression.string("true")));
            ResultSet rs = query.execute();
            int size = rs.allResults().size();
            //The result should only be one since only one person is logged in to device
            if( size == 1){
                rs = query.execute();
                //get doc ID to open this user profile
                String userDocId = rs.allResults().get(0).getString("userDocId");
                Document userDoc = userDatabase.getDocument(userDocId);

                //load login user's information
                String username = userDoc.getString("username");
                String UUID = userDoc.getString("UUID");
                myUUID = UUID;
                myUsername = username;

                final Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("UserDocId", userDocId);
                intent.putExtra("UUID", UUID);
                intent.putExtra("Username", username);

                //wait for 2 second to show logo, and direct to next activity
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }else if( size > 1){
                //in case there is any bug that multiple users log in, forced to log them out.
                for(int i = 0; i < size; i++){
                    rs = query.execute();
                    String userDocId = rs.allResults().get(i).getString("userDocId");
                    MutableDocument userDoc = userDatabase.getDocument(userDocId).toMutable();

                    userDoc.setString("hasLogin", "false");
                    userDatabase.save(userDoc);
                }
                final Intent intent = new Intent(this, LoginPageActivity.class);
                //wait 2 second
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }else{
                //no user has log in
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
