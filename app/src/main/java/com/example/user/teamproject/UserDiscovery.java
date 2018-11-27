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
    ProgressDialog progressDialog;

    // TODO: Add a re-search button and link it to initiating search again

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

    List<String> selectNameArray = new ArrayList<String>();
    List<WifiP2pDevice> selectDeviceArray = new ArrayList<WifiP2pDevice>();

    // Service arrays
    List<WiFiP2pService> serviceList = new ArrayList<WiFiP2pService>();
    List<String> serviceNameArray = new ArrayList<String>();
    private WifiP2pDnsSdServiceRequest serviceRequest;
    final Map<String, String> deviceToUUID = new HashMap<String, String>();
    final Map<String, String> deviceToUsername = new HashMap<String, String>();
    WiFiP2pService selectedService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_discovery);

        //Wire UI to Variables
        wireUiToVars();
        setupObjects();

        // Check if WiFi is already enabled
        if (wifiManager.isWifiEnabled()) {
//            Toast.makeText(this, "WiFi already enabled!", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "WiFi needed for this app to work. WiFi will automatically be turned on.", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

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


//        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                searchStatus.setText("Searching...");
//            }
//
//            @Override
//            public void onFailure(int reason) {
//                searchStatus.setText("Discovery starting failed");
//            }
//        });
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                final WifiP2pDevice device = selectDeviceArray.get(position);
//                String deviceAddress = device.deviceAddress;
//
//                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
//                intent.putExtra("deviceName", device.deviceName);
//                intent.putExtra("deviceAddress", deviceAddress);
//                intent.putExtra("deviceType", "host");
//
//                startActivity(intent);
//            }
//        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                WiFiP2pService service = serviceList.get(position);
                connectP2p(service);
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

//    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
//        @Override
//        public void onPeersAvailable(WifiP2pDeviceList peerList) {
//            if (!peerList.getDeviceList().equals(peers)) {
//                peers.clear();
//                peers.addAll(peerList.getDeviceList());
//
//                selectNameArray.clear();
//                selectDeviceArray.clear();
//
//                deviceNameArray = new String[peerList.getDeviceList().size()];
//                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
//
//                int index = 0;
//                int selectCounter = 0;
//                for (WifiP2pDevice device : peerList.getDeviceList()) {
//                    String devName = device.deviceName;
//                    deviceNameArray[index] = device.deviceName;
//                    deviceArray[index] = device;
//                    index++;
//                    if (devName.toLowerCase().contains("here")) {
//                        selectNameArray.add(devName.substring(5));
//                        selectDeviceArray.add(device);
//                        selectCounter++;
//                    }
//                }
//
//                Log.d("User Discovery", "index: " + index);
//
//
//                if (selectCounter > 0) {
//                    // TODO: add to recycler view
//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, selectNameArray);
//                    listView.setAdapter(adapter);
//
//                    searchStatus.setText(selectCounter + " nearby user(s) found. Click on a user to start chatting.");
//                } else {
//                    searchStatus.setText("No nearby users found. Try again later.");
//                }
//            }
//
//            if (peerList.getDeviceList().size() == 0) {
//                searchStatus.setText("No nearby users found. Try again later.");
//                return;
//            }
//        }
//    };

//    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
//        @Override
//        public void onConnectionInfoAvailable(WifiP2pInfo info) {
//            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
//
//            if (info.groupFormed && info.isGroupOwner) {
////                Toast.makeText(getApplicationContext(), "Host", Toast.LENGTH_SHORT).show();
//
//            } else if (info.groupFormed && !info.isGroupOwner) {
////                Toast.makeText(getApplicationContext(), "Client formed", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
//                intent.putExtra("deviceName", "here New Device");
//                intent.putExtra("deviceAddress", groupOwnerAddress);
//                intent.putExtra("deviceType", "client");
//                startActivity(intent);
//                finish();
//            }
//        }
//    };

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
                    // TODO: Add the service to the list
//
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
//                Toast.makeText(getApplicationContext(), "Service Discovery Request Added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
//                Toast.makeText(getApplicationContext(), "Failed to add Service Discovery request", Toast.LENGTH_SHORT).show();
            }
        });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                searchStatus.setText("Searching for nearby users...");
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
                // TODO: Move to onConnectionInfoAvailable
//                Intent intent = new Intent(getApplicationContext(), ChatterActivity.class);
//                intent.putExtra("service", selectedService);
//                startActivity(intent);
//                finish();
            }
            @Override
            public void onFailure(int errorCode) {
                searchStatus.setText("Failed connecting to service");
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        Thread handler = null;
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */
        if (p2pInfo.groupFormed) {
            if (p2pInfo.isGroupOwner) {
                Log.d("Host", "Connected as group owner");
                //            try {
                //                handler = new GroupOwnerSocketHandler(
                //                        ((MessageTarget) this).getHandler());
                //                handler.start();
                //            } catch (IOException e) {
                //                Log.d(TAG,
                //                        "Failed to create a server thread - " + e.getMessage());
                //                return;
                //            }
                Intent intent = new Intent(getApplicationContext(), ChatterActivity.class);
                intent.putExtra("service", selectedService);
                intent.putExtra("deviceType", "host");
                startActivity(intent);
                finish();
            } else {
                //            Log.d(TAG, "Connected as peer");
                //            handler = new ClientSocketHandler(
                //                    ((MessageTarget) this).getHandler(),
                //                    p2pInfo.groupOwnerAddress);
                //            handler.start();
                //        }
                //        chatFragment = new WiFiChatFragment();
                //        getFragmentManager().beginTransaction()
                //                .replace(R.id.container_root, chatFragment).commit();
                //        statusTxtView.setVisibility(View.GONE);
                Log.d("P2PInfoClient", p2pInfo.toString());
                Intent intent = new Intent(getApplicationContext(), ChatterActivity.class);
                WiFiP2pService service = new WiFiP2pService();
//                service.setUuid(deviceToUUID.get(p2pInfo.groupOwnerAddress));
//                service.setUsername(deviceToUsername.get(p2pInfo.groupOwnerAddress));
                service.setGroupOwnerAddress(p2pInfo.groupOwnerAddress);
                service.setDeviceType("client");
                intent.putExtra("service", service);
                intent.putExtra("deviceType", "client");
                startActivity(intent);
                finish();
            }
        }
    }
}
