package com.example.user.teamproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private UserDiscovery userDiscoveryActivity;
    private ChatterActivity chatActivity;
    private HomeActivity homeActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, UserDiscovery userDiscoveryActivity) {
        this.mManager = manager;
        this.mChannel = channel;
        this.userDiscoveryActivity = userDiscoveryActivity;
        this.chatActivity = null;
        this.homeActivity = null;
    }

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ChatterActivity chatActivity) {
        this.mManager = manager;
        this.mChannel = channel;
        this.chatActivity = chatActivity;
        this.userDiscoveryActivity = null;
        this.homeActivity = null;
    }

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, HomeActivity homeActivity) {
        this.mManager = manager;
        this.mChannel = channel;
        this.chatActivity = null;
        this.userDiscoveryActivity = null;
        this.homeActivity = homeActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // TODO: Remove unnecessary toasts
                Toast.makeText(context, "Wifi is On", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Wifi is Off", Toast.LENGTH_SHORT).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {

                if (userDiscoveryActivity != null) {

                } else {
                    // do nothing
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (userDiscoveryActivity != null) {
                    if (networkInfo.isConnected()) {
                        mManager.requestConnectionInfo(mChannel, (WifiP2pManager.ConnectionInfoListener) userDiscoveryActivity);
                    } else {
                        Toast.makeText(userDiscoveryActivity.getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                } else if (chatActivity != null) {
                    if (networkInfo.isConnected()) {
                        mManager.requestConnectionInfo(mChannel, (WifiP2pManager.ConnectionInfoListener) chatActivity);
                    } else {
                        Toast.makeText(chatActivity.getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                } else if (homeActivity != null) {
                    if (networkInfo.isConnected()) {
                        mManager.requestConnectionInfo(mChannel, (WifiP2pManager.ConnectionInfoListener) homeActivity);
                    } else {
                        Toast.makeText(homeActivity.getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                return;
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }
}
