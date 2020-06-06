package com.pc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/** Thread representing a server with a port. */
public class Server extends Thread {

    /** This server's port. */
    private final int serverPort;

    /** A list of all workers on this server. */
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    /** Constructor for this server. Takes in a port SERVERPORT. */
    public Server (int serverPort) {
        this.serverPort = serverPort;
    }

    /** Returns worker list of this server. */
    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException ex) {
            System.out.println("IOException.");
        }
    }

    /** Removes a worker SERVERWORKER from this server's worker list. */
    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
