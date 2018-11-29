package com.example.user.teamproject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerClass extends Thread {
    Socket socket;
    ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8888);
            socket = serverSocket.accept();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
