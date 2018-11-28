package com.example.user.teamproject;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.MutableDocument;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.example.user.teamproject.InitiateActivity.SERVICE_INSTANCE;
import static com.example.user.teamproject.InitiateActivity.SERVICE_REG_TYPE;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, WifiP2pManager.ConnectionInfoListener {
    private RecyclerView mRecyclerView;
    private FriendListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ImageView HomeImage, status;
    TextView HomeUUID, HomeUsername;
    String friendUsername, friendUUID;
    FloatingActionButton fab;
    FloatingActionButton fab2;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    String myUUID;
    String myUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final ArrayList<Friend_card> friendList = new ArrayList<>();

        //remove later
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String ago = prettyTime.format(new Date(String.valueOf(Calendar.getInstance().getTime())));
        int connection = 1;
        friendList.add(new Friend_card("kles", "kles835135248", ago, connection));

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
                Intent intent = new Intent(HomeActivity.this, ChatterActivity.class);
                intent.putExtra("friendUsername", friendUsername);
                intent.putExtra("friendUUID", friendUUID);

                startActivity(intent);
            }
        });

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(getApplicationContext(), UserDiscovery.class);
                startActivity(intent);
            }
        });

        fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create Group for WiFi Direct
                createWifiP2pGroup();
            }
        });

        //navigation drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //user's information
        Intent intent = getIntent();

        HomeImage = navigationView.getHeaderView(0).findViewById(R.id.NavHeaderImageView);
        byte[] imageInByte = intent.getByteArrayExtra("ProfileImage");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageInByte, 0, imageInByte.length);
        HomeImage.setImageBitmap(bitmap);

        HomeUUID = navigationView.getHeaderView(0).findViewById(R.id.NavHeaderUUID);
        HomeUUID.setText(intent.getStringExtra("UUID"));
        HomeUsername = navigationView.getHeaderView(0).findViewById(R.id.NavHeaderUsername);
        HomeUsername.setText(intent.getStringExtra("Username"));

        //open exist chat
        try {
            // Get the database (and create it if it doesn’t exist).
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            Database friendDatabase = new Database("friendList", config);

            /*
            * //list all friend
            * final ArrayList<Friend_card> friendList = new ArrayList<>();
            *
            * Query query = QueryBuilder.select(SelectResult.property("friendUUID"))
                                .from(DataSource.database(friendDatabase))
                                .where(Expression.property("friendUUID"));
            * rs = query.execute();
            * int i = 0, size = rs.allResults().size();;
            * String referChatRoom;
            * while( i < rs.allResults().size()){
            *   rs = query.execute();
            *   friendUUID = rs.allResults().get(i).getString("friendUUID");
            *   rs = query.execute();
            *   friendUsername = rs.allResults().get(i).getString("friendUsername");
            *   rs = query.execute();
            *   time = rs.allResults().get(i).getString("time");
            *   int connection = 1;
            *   friendList.add(new Friend_card(friendUsername, friendUUID, time, connection));
            *   i++;
            * }
            *
            * mRecyclerView = findViewById(R.id.recyclerView);
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
                    Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                    intent.putExtra("FriendUsername", friendUsername);
                 intent.putExtra("FriendUUID", friendUUID);

                 startActivity(intent);
                }
                });
             */
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
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

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.action_settings) {

        } else if (id == R.id.nav_logout) {
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

    public void createWifiP2pGroup() {
        startRegistration();

        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast toast = Toast.makeText(getApplicationContext(), "Group Created", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }

            @Override
            public void onFailure(int reason) {
                Toast toast = Toast.makeText(getApplicationContext(), "Group Created", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        });
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
                Toast.makeText(getApplicationContext(),"Successfully added " + myUUID, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int error) {
                Toast.makeText(getApplicationContext(),"Failed to add service", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info.groupFormed) {
            if (info.isGroupOwner) {
                Intent intent = new Intent(getApplicationContext(), ChatterActivity.class);
                WiFiP2pService service = new WiFiP2pService();
                intent.putExtra("service", service);
                intent.putExtra("deviceType", "groupOwner");
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}
