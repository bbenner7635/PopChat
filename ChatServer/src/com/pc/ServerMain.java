package com.pc;

/** Main class for chat server. */
public class ServerMain {

    /** Creates server socket and listens for multiple connections. */
    public static void main(String[] args) {
        int port = 7635;
        Server server = new Server(port);
        server.start();
    }
}
