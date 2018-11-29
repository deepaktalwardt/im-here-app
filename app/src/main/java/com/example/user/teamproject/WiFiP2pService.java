package com.example.user.teamproject;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * A structure to hold service information.
 */
public class WiFiP2pService implements Serializable {
    String deviceName;
    String deviceAddress;
    String instanceName = null;
    String serviceRegistrationType = null;
    String uuid = null;
    String username = null;
    InetAddress groupOwnerAddress = null;
    String deviceType;

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGroupOwnerAddress(InetAddress address) {
        this.groupOwnerAddress = address;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getDeviceAddress() {
        return this.deviceAddress;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public InetAddress getGroupOwnerAddress() {
        return this.groupOwnerAddress;
    }
}
