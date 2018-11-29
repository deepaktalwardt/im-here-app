package com.example.user.teamproject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
