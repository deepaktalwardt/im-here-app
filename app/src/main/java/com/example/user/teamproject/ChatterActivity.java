package com.example.user.teamproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChatterActivity extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener {

    // Constants
    final String tag = "ChatterActivity";

    // UI elements
    FloatingActionButton sendButton;
    FloatingActionButton arButton;
    EditText entryBox;
    ListView messageList;

    // WiFi Connection Objects
    WiFiP2pService service = null;
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    SendReceive sendReceive;
    static final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    String deviceType;
    InetAddress groupOwnerAddress;

    // Friend Identifiers
    String docID, friendUUID, friendUsername;

    // My identity params
    String selfUsername;
    String selfUUID;

    // Databases
    Database userListDatabase;
    Database chatRoomDatabase;

    // Chat Objects
    ArrayList msgList = new ArrayList<ChatModel>();
    CustomAdapter adapter;
    Vibrator v;

    // Connection type
    String connectionType;

    // Friend Location
    String friendLon = null;
    String friendLat = null;
    Boolean metaSent = false;

    // My Location
    LocationManager locationManager;
    Double myLat = 37.335890;
    Double myLon = -121.882578;
    Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatter);

        populateSelfParams();
        wireUiToVars();
        setupObjects();
        setupLocation();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // TODO: Change how Username and UUID is set on action bar
        Intent intent = getIntent();
        service = (WiFiP2pService) intent.getSerializableExtra("service");
        deviceType = intent.getStringExtra("deviceType");

        if (deviceType.equals("initConnection")) {
            initiateConnection("initConnection", service);
            friendUUID = service.getUuid();
            friendUsername = service.getUsername();
        } else if (deviceType.equals("recvConnection")) {
            groupOwnerAddress = service.getGroupOwnerAddress();
            initiateConnection("client", service);
        }

        setSendButtonOnClickListener();
        setARButtonOnClickListener();
    }

    private void wireUiToVars() {
        sendButton = (FloatingActionButton) findViewById(R.id.send_fab);
        entryBox = (EditText) findViewById(R.id.entryBox);
        messageList = (ListView) findViewById(R.id.messageList);
        arButton = (FloatingActionButton) findViewById(R.id.ar_fab);
    }

    private void setupObjects() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setSendButtonOnClickListener() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String metadata = createJSONMeta(selfUUID, selfUsername);
                    sendReceive.write(metadata.getBytes());
                    metaSent = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Something went wrong, please try again or restart the app.", Toast.LENGTH_LONG).show();
                }

                String textToSend = entryBox.getText().toString();

                try {
                    String packetToSend = createJSONChat(textToSend, myLat.toString(), myLon.toString());
                    sendReceive.write(packetToSend.getBytes());
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));


                    ChatModel model = new ChatModel(textToSend, true);
                    adapter = new CustomAdapter(getApplicationContext(), msgList);
                    messageList.setAdapter(adapter);
                    entryBox.setText("");
                    msgList.add(model);
                    adapter.notifyDataSetChanged();

                    //create new or open exist chat db by using UUID
                    try {
                        DatabaseConfiguration DBconfig = new DatabaseConfiguration(getApplicationContext());
                        chatRoomDatabase = new Database(friendUUID, DBconfig);

                        //Every message will be a new doc
                        MutableDocument messageModel = new MutableDocument();
                        int count = (int) chatRoomDatabase.getCount();
                        //count can be last index
                        //such as when count is 0, means there is no previous message
                        //and the current message will be at index 0 to new chat db.
                        //and so on.
                        JSONObject sentMessage = new JSONObject(packetToSend);
                        messageModel.setString("chatType", "send");
                        messageModel.setInt("index", count);
                        messageModel.setValue("time", Calendar.getInstance().getTime());
                        messageModel.setString("message", sentMessage.toString());
                        chatRoomDatabase.save(messageModel);
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong, please try again or restart the app.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void setARButtonOnClickListener() {
        arButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friendLat != null && friendLon != null) {
                    Intent intent = new Intent(getApplicationContext(), ARActivity.class);
                    intent.putExtra("targetLat", friendLat);
                    intent.putExtra("targetLon", friendLon);
                    intent.putExtra("username", friendUsername);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Location from friend not yet received. No GPS signal.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void setupLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                myLat = location.getLatitude();
                myLon = location.getLongitude();
                myLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListenerGPS);
    }

    private void initiateConnection(String connectionType, WiFiP2pService service) {
        if (connectionType.equals("initConnection")) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = service.getDeviceAddress();
            config.wps.setup = WpsInfo.PBC;

            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection Request Successful", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Connection Request Unsuccessful", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
            });
        } else if (connectionType.equals("recvConnection")) {

        }
    }

    private void populateSelfParams() {
        // Get own information from userList Database and save to constants
        try {
            DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
            userListDatabase = new Database("userList", config);

            Query query = QueryBuilder.select(SelectResult.property("username"), SelectResult.property("UUID"), SelectResult.property("hasLogin"))
                    .from(DataSource.database(userListDatabase))
                    .where(Expression.property("hasLogin").equalTo(Expression.string("true")));

            com.couchbase.lite.ResultSet rs = query.execute();
            for (Result result : rs) {
                if (result.getString("hasLogin").equals("true")) {
                    selfUsername = result.getString("username");
                    selfUUID = result.getString("UUID");
                }
                Log.d(tag, "username: " + result.getString("username"));
                Log.d(tag, "UUID: " + result.getString("UUID"));
                Log.d(tag, "hasLogin: " + result.getString("hasLogin"));
            }

        } catch (CouchbaseLiteException ce) {
            Log.d(tag, "Couldn't retrieve from the database");
            ce.printStackTrace();
        }
    }

    private class SendReceive extends Thread {

        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt) {
            socket = skt;
            Log.d("SendReceiveCreated", "SendReceiveCreated");
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);

                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new ChatterActivity.SendReceive(socket);
                sendReceive.start();
                Log.d("ServerThreadStarted", "Group Owner");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void closeSocket() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientClass extends Thread {

        Socket socket;
        String hostAdd;

        public ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                sendReceive = new ChatterActivity.SendReceive(socket);
                sendReceive.start();
                Log.d("ClientThreadStarted", "Client");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void closeSocket() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
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
    }

    Handler handler = new Handler(new Handler.Callback() {
        MutableDocument friendDoc;

        @Override
        public boolean handleMessage(Message msg) {
            DatabaseConfiguration DBconfig = new DatabaseConfiguration(getApplicationContext());
            switch (msg.what) {
                case MESSAGE_READ:
                    try {
                        byte[] readBuff = (byte[]) msg.obj;
                        String tempMsg = new String(readBuff, 0, msg.arg1);
                        Toast.makeText(getApplicationContext(), tempMsg, Toast.LENGTH_LONG).show();

                        try {
                            JSONObject parsedMessage = new JSONObject(tempMsg);
                            if (parsedMessage.get("type").equals("meta")) {
                                friendUsername = parsedMessage.getString("username");
                                friendUUID = parsedMessage.getString("UUID");
                                getSupportActionBar().setTitle(friendUsername);

                                //open friendList db to store friend
                                // Get the database (and create it if it doesn’t exist).
                                try {
                                    userListDatabase = new Database("friendList", DBconfig);

                                    //check if this friendUUID is in friend list
                                    Query query = QueryBuilder.select(SelectResult.property("friendUUID"))
                                            .from(DataSource.database(userListDatabase))
                                            .where(Expression.property("friendUUID").equalTo(Expression.string(friendUUID)));
                                    ResultSet rs = query.execute();

                                    //if this is a new friend, creates a new doc to store this new friend
                                    if (rs.allResults().size() == 0) {
                                        friendDoc = new MutableDocument();
                                        docID = friendDoc.getId();
                                        friendDoc.setString("docID", docID);
                                        friendDoc.setString("friendUUID", friendUUID);
                                        friendDoc.setString("friendUsername", friendUsername);
                                        userListDatabase.save(friendDoc);
                                        Toast toast = Toast.makeText(ChatterActivity.this, "friendList added", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                        toast.show();
                                    }else{
                                        Toast toast = Toast.makeText(ChatterActivity.this, "friendList added already", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                        toast.show();
                                    }
                                } catch (CouchbaseLiteException e) {
                                    e.printStackTrace();
                                }

                            } else if (parsedMessage.get("type").equals("chat")) {
                                String message = parsedMessage.getString("message");
                                friendLon = parsedMessage.getString("lon");
                                friendLat = parsedMessage.getString("lat");
                                // Get instance of Vibrator from current Context
                                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));

                                ChatModel model = new ChatModel(message, false);
                                adapter = new CustomAdapter(getApplicationContext(), msgList);
                                messageList.setAdapter(adapter);
                                msgList.add(model);
                                adapter.notifyDataSetChanged();

                                //create new or open exist chat db by using UUID
                                try {
                                    chatRoomDatabase = new Database(friendUUID, DBconfig);

                                    //Every message will be a new doc
                                    MutableDocument messageModel = new MutableDocument();
                                    int count = (int) chatRoomDatabase.getCount();
                                    //count can be last index
                                    //such as when count is 0, means there is no previous message
                                    //and the current message will be at index 0 to new chat db.
                                    messageModel.setString("chatType", "receive");
                                    messageModel.setInt("index", count);
                                    messageModel.setString("message", parsedMessage.toString());
                                    chatRoomDatabase.save(messageModel);

                                    //open friendDoc every time that message sent
                                    Query query = QueryBuilder.select(SelectResult.property("docID"))
                                            .from(DataSource.database(userListDatabase))
                                            .where(Expression.property("friendUUID").equalTo(Expression.string(friendUUID)));
                                    ResultSet rs = query.execute();
                                    docID = rs.allResults().get(0).getString("docID");
                                    friendDoc = userListDatabase.getDocument(docID).toMutable();
                                    //get time
                                    Date time = Calendar.getInstance().getTime();
                                    friendDoc.setValue("time", time);
                                    userListDatabase.save(friendDoc);
                                } catch (CouchbaseLiteException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Something went wrong, please try again or restart the app.", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
            return true;
        }
    });

    private String createJSONMeta(String uuid, String username) throws JSONException {
        try {
            JSONObject meta = new JSONObject();
            meta.put("type", "meta");
            meta.put("UUID", uuid);
            meta.put("username", username);
            Log.d("Chat Activity", "meta: " + meta.toString());
            return meta.toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String createJSONChat(String msg, String lat, String lon) throws JSONException {
        try {
            JSONObject chat = new JSONObject();
            chat.put("type", "chat");
            chat.put("lat", lat);
            chat.put("lon", lon);
            chat.put("message", msg);
            return chat.toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        final InetAddress goAddress = info.groupOwnerAddress;
        if (info.groupFormed && info.isGroupOwner) {
            if (serverClass == null) {
                serverClass = new ServerClass();
                serverClass.start();
            }
            // TODO: Send data about myself to the client
            getSupportActionBar().setTitle("New Device");
        } else if (info.groupFormed && !info.isGroupOwner) {
            Log.d("Chat Activity", "goAddress populated");
            groupOwnerAddress = goAddress;
            if (clientClass == null) {
                clientClass = new ClientClass(goAddress);
                clientClass.start();
            }
            getSupportActionBar().setTitle("Group Owner@" + groupOwnerAddress.toString());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
