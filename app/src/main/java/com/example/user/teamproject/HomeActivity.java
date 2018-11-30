package com.example.user.teamproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.hardware.camera2.CameraDevice;

import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.example.user.teamproject.InitiateActivity.SERVICE_INSTANCE;
import static com.example.user.teamproject.InitiateActivity.SERVICE_REG_TYPE;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView mRecyclerView;
    private FriendListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    TextView HomeUUID, HomeUsername;
    String friendUsername, friendUUID;
    FloatingActionButton fab;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    String myUUID;
    String myUsername;

    Date time;

    private CameraDevice cameraDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Check if WiFi is already enabled
        if (wifiManager.isWifiEnabled()) {

        } else {
            wifiManager.setWifiEnabled(true);
        }

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserDiscovery.class);
                startActivity(intent);

            }
        });

        //navigation drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chats");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //user's information
        Intent intent = getIntent();;

        HomeUUID = navigationView.getHeaderView(0).findViewById(R.id.NavHeaderUUID);
        HomeUUID.setText(intent.getStringExtra("UUID"));
        HomeUsername = navigationView.getHeaderView(0).findViewById(R.id.NavHeaderUsername);
        HomeUsername.setText(intent.getStringExtra("Username"));

        populateOldChat();

        startRegistration();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            try {
                // Get the database (and create it if it doesn’t exist).
                DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
                Database userDatabase = new Database("userList", config);
                Intent intent = getIntent();
                String userDocId = intent.getStringExtra("UserDocId");
                MutableDocument userDoc = userDatabase.getDocument(userDocId).toMutable();
                userDoc.setString("hasLogin", "false");
                userDatabase.save(userDoc);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(HomeActivity.this, LoginPageActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistration() {
        Intent intent = getIntent();
        myUUID = intent.getStringExtra("UUID");
        myUsername = intent.getStringExtra("Username");

        final Map<String, String> record = new HashMap<String, String>();
        record.put("available", "visible");
        record.put("Name", "imhere!!!");
        record.put("myUUID", myUUID);
        record.put("myUsername", myUsername);

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);

        mManager.addLocalService(mChannel, service, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Successfully added " + myUUID, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int error) {
                Toast.makeText(getApplicationContext(), "Failed to add service", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void populateOldChat(){
        final ArrayList<Friend_card> friendList = new ArrayList<>();
        //open exist chat
        try {
            // Get the database (and create it if it doesn’t exist).
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            Database friendDatabase = new Database("friendList", config);

            //list all friend
            Query query = QueryBuilder.select(SelectResult.property("friendUUID"), SelectResult.property("friendUsername"))
                    .from(DataSource.database(friendDatabase));
            ResultSet rs = query.execute();
            int size = rs.allResults().size();
            Toast toast = Toast.makeText(HomeActivity.this, "friendList size:" + size, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();

            for(int i = 0; i < size; i++) {
                rs = query.execute();
                friendUUID = rs.allResults().get(i).getString("friendUUID");
                rs = query.execute();
                friendUsername = rs.allResults().get(i).getString("friendUsername");
                rs = query.execute();

                try {
                    Database chatRoomDatabase;
                    chatRoomDatabase = new Database(friendUUID, config);
                    Query q2 = QueryBuilder.select(SelectResult.property("index"), SelectResult.property("time"))
                            .from(DataSource.database(chatRoomDatabase));

                    ResultSet rsChat = q2.execute();
                    int rsChatSize = rsChat.allResults().size();

                    friendList.add(new Friend_card(friendUsername, friendUUID, "3:17 pm"));

                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }

            mRecyclerView = findViewById(R.id.recyclerView);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            mAdapter = new FriendListAdapter(friendList);

            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);

            mAdapter.setOnItemClickListener(new FriendListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    friendUsername = friendList.get(position).getUsername();
                    friendUUID = friendList.get(position).getUUID();
                    Intent intent = new Intent(HomeActivity.this, OldMessageActivity.class);
                    intent.putExtra("FriendUsername", friendUsername);
                    intent.putExtra("FriendUUID", friendUUID);
                    startActivity(intent);
                }
            });
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateOldChat();
    }
}
