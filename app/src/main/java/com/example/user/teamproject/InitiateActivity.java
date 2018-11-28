package com.example.user.teamproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.Map;

public class InitiateActivity extends AppCompatActivity {

    private WifiP2pManager manager;
    static final int SERVER_PORT = 4545;

    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;

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

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);


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
                myUUID = UUID;
                myUsername = username;

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

//        startRegistration();

    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistration() {
        final Map<String, String> record = new HashMap<String, String>();
        record.put("available", "visible");
        record.put("Name", "imhere!!!");
        record.put("myUUID", myUUID);
        record.put("myUsername", myUsername);

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);

        manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),"Successfully added " + myUUID, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int error) {
                Toast.makeText(getApplicationContext(),"Failed to add service", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
