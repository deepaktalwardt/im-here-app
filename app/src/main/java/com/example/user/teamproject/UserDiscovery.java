package com.example.user.teamproject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class UserDiscovery extends AppCompatActivity {
    // UI elements
//    RecyclerView searchResultView;
    ListView listView;
    TextView searchStatus;
    ProgressDialog progressDialog;

    //TODO: Add a re-search button and link it to initiating search again

    // TODO: Figure out how to change WiFi Direct device Name from app

    // Objects
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

//    String[] selectNameArray;
//    WifiP2pDevice[] selectDeviceArray;

    List<String> selectNameArray = new ArrayList<String>();
    List<WifiP2pDevice> selectDeviceArray = new ArrayList<WifiP2pDevice>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_discovery);

        //Wire UI to Variables
        wireUiToVars();
        setupObjects();

        // Check if WiFi is already enabled
        if (wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi already enabled!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "WiFi needed for this app to work. WiFi will automatically be turned on.", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                searchStatus.setText("Searching...");
            }

            @Override
            public void onFailure(int reason) {
                searchStatus.setText("Discovery starting failed");
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final WifiP2pDevice device = selectDeviceArray.get(position);
//                final WifiP2pConfig config = new WifiP2pConfig();
//                config.deviceAddress = device.deviceAddress;
                String deviceAddress = device.deviceAddress;

                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("deviceName", device.deviceName);
//                intent.putExtra("config", config);
                intent.putExtra("deviceAddress", deviceAddress);
                intent.putExtra("deviceType", "host");

                startActivity(intent);

//                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
//                    @Override
//                    public void onSuccess() {
//                        Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
//
//                        // TODO: Add Intent to open the correct activity
//
//
//                        // TODO: Add to Couchbase DB
//                    }
//
//                    @Override
//                    public void onFailure(int reason) {
//                        Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
//                    }
//                });



            }
        });

    }

    private void setupObjects() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void wireUiToVars() {
//        searchResultView = (RecyclerView) findViewById(R.id.searchResultView);
        listView = (ListView) findViewById(R.id.listView);
        searchStatus = (TextView) findViewById(R.id.searchStatus);
        getSupportActionBar().setTitle("Nearby Users");
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                selectNameArray.clear();
                selectDeviceArray.clear();

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;
                int selectCounter = 0;
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    String devName = device.deviceName;
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                    if (devName.toLowerCase().contains("here")) {
                        selectNameArray.add(devName.substring(5));
                        selectDeviceArray.add(device);
                        selectCounter++;
                    }
                }

                Log.d("User Discovery", "index: " + index);


                if (selectCounter > 0) {
                    // TODO: add to recycler view
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, selectNameArray);
                    listView.setAdapter(adapter);

                    searchStatus.setText(selectCounter + " nearby user(s) found. Click on a user to start chatting.");
                } else {
                    searchStatus.setText("No nearby users found. Try again later.");
                }
            }

            if (peerList.getDeviceList().size() == 0) {
                searchStatus.setText("No nearby users found. Try again later.");
                return;
            }
        }
    };

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.groupFormed && info.isGroupOwner) {
//                Toast.makeText(getApplicationContext(), "Host", Toast.LENGTH_SHORT).show();

            } else if (info.groupFormed && !info.isGroupOwner) {
//                Toast.makeText(getApplicationContext(), "Client formed", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("deviceName", "here New Device");
                intent.putExtra("deviceAddress", groupOwnerAddress);
                intent.putExtra("deviceType", "client");
                startActivity(intent);
                finish();
            }
        }
    };

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
