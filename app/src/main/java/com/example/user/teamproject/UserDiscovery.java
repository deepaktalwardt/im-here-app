package com.example.user.teamproject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.user.teamproject.InitiateActivity.SERVICE_INSTANCE;
import static com.example.user.teamproject.InitiateActivity.TXTRECORD_PROP_AVAILABLE;

public class UserDiscovery extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener {
    // UI elements
    ListView listView;
    TextView searchStatus;

    // Objects
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    // Service arrays
    List<WiFiP2pService> serviceList = new ArrayList<WiFiP2pService>();
    List<String> serviceNameArray = new ArrayList<String>();
    private WifiP2pDnsSdServiceRequest serviceRequest;
    final Map<String, String> deviceToUUID = new HashMap<String, String>();
    final Map<String, String> deviceToUsername = new HashMap<String, String>();
    WiFiP2pService selectedService;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_discovery);

        //Wire UI to Variables
        wireUiToVars();
        setupObjects();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Check if group already created, if yes, delete it
        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null) {
                    mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast toast = Toast.makeText(getApplicationContext(), "Previous Group successfully removed", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Failed to remove group: " + reason, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Group Does not exist", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
            }
        });

        discoverService();
        pb.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                WiFiP2pService service = serviceList.get(position);
                selectedService = service;
                Intent intent = new Intent(getApplicationContext(), ChatterActivity.class);
                intent.putExtra("service", selectedService);
                intent.putExtra("deviceType", "initConnection");
                startActivity(intent);
                finish();
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
        listView = (ListView) findViewById(R.id.listView);
        searchStatus = (TextView) findViewById(R.id.searchStatus);
        getSupportActionBar().setTitle("Nearby Users");
        pb = (ProgressBar) findViewById(R.id.pb);
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

    private void discoverService() {
        serviceList.clear();
        serviceNameArray.clear();

        WifiP2pManager.DnsSdServiceResponseListener dnsSdServiceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                    if (deviceToUUID.containsKey(srcDevice.deviceAddress)) {
                        WiFiP2pService service = new WiFiP2pService();
                        service.setDeviceName(srcDevice.deviceName);
                        service.setDeviceAddress(srcDevice.deviceAddress);
                        service.instanceName = instanceName;
                        service.serviceRegistrationType = registrationType;
                        service.setUuid(deviceToUUID.get(srcDevice.deviceAddress));
                        service.setUsername(deviceToUsername.get(srcDevice.deviceAddress));
                        serviceList.add(service);
                        serviceNameArray.add(srcDevice.deviceName + "(" + deviceToUUID.get(srcDevice.deviceAddress) + ")");
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, serviceNameArray);
                        listView.setAdapter(adapter);
                    }
                    Log.d("deviceToUUID", deviceToUUID.toString());
                    Log.d("deviceToUsername", deviceToUsername.toString());
                }
            }
        };

        WifiP2pManager.DnsSdTxtRecordListener dnsSdTxtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                deviceToUUID.put(srcDevice.deviceAddress, txtRecordMap.get("myUUID"));
                deviceToUsername.put(srcDevice.deviceAddress, txtRecordMap.get("myUsername"));
                Log.d("User Discovery UUID", srcDevice.deviceName + " is " + txtRecordMap.get("myUUID"));
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, dnsSdServiceResponseListener, dnsSdTxtRecordListener);

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                searchStatus.setText("Searching...");
//                Toast.makeText(getApplicationContext(), "Service Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                searchStatus.setText("Searching failed");
//                Toast.makeText(getApplicationContext(), "Service Discovery Could not be initiated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void connectP2p(WiFiP2pService service) {
        selectedService = service;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.getDeviceAddress();
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;
        if (serviceRequest != null)
            mManager.removeServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onFailure(int arg0) {
                        }
                    });
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                searchStatus.setText("Connecting to service...");
            }
            @Override
            public void onFailure(int errorCode) {
                searchStatus.setText("Failed connecting to service");
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        if (p2pInfo.groupFormed) {
            if (p2pInfo.isGroupOwner) {
                Log.d("Host", "Connected as group owner");
                Intent intent = new Intent(getApplicationContext(), ChatterActivity.class);
                WiFiP2pService service = new WiFiP2pService();
                intent.putExtra("service", service);
                intent.putExtra("deviceType", "recvConnection");
                service.setGroupOwnerAddress(p2pInfo.groupOwnerAddress);
                startActivity(intent);
                finish();
            } else {
//                Log.d("P2PInfoClient", p2pInfo.toString());
                Intent intent = new Intent(getApplicationContext(), ChatterActivity.class);
                WiFiP2pService service = new WiFiP2pService();
                service.setGroupOwnerAddress(p2pInfo.groupOwnerAddress);
                intent.putExtra("service", service);
                intent.putExtra("deviceType", "recvConnection");
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
