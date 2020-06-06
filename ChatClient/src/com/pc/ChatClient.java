package com.pc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.sun.tools.internal.ws.wsdl.document.Message;
import org.apache.commons.lang3.*;

/** Class for the chat client. */
public class ChatClient {

    /** Server name for this client. */
    private final String serverName;

    /** Server port for client's server. */
    private final int serverPort;

    /** Socket of this client. */
    private Socket socket;

    /** OutputStream of this client's server. */
    private OutputStream serverOut;

    /** OutputStream of this client's server. */
    private InputStream serverIn;

    /** Buffered reader for this client. */
    private BufferedReader bufferedIn;

    /** List of listeners for user statuses. */
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();

    /** List of listeners for messages. */
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    /** Constructor for the chat client. */
    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    /** Creates a client. */
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 7635);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });
        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from " + fromLogin + " " +
                        "===> " + msgBody);
            }
        });
        if (!client.connect()) {
            System.err.println("Connection failed.");
        } else {
            System.err.println("Connection successful.");
            if (client.login("guest", "guest")) {
                System.out.println("Login successful.");
                client.msg("guest", "Hello world");
            } else {
                System.out.println("Login failed.");
            }
//            client.logoff();
        }
    }

    /** Sends a message MSGBODY to user SENDTO. */
    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    /** Logs in the client with login LOGIN and password PASSWORD. */
    public boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());
        String response = bufferedIn.readLine();
        System.out.println("Response Line: " + response);
        if ("ok login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    /** Starts the message reader. */
    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    /** Reads a message in a loop. */
    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception.");
            try {
                socket.close();
            } catch (IOException ex1) {
                System.out.println("IOException.");
            }
        }
    }

    /** Handles the message command for the client. */
    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];
        for (MessageListener listener : messageListeners) {
            listener.onMessage(login, msgBody);
        }
    }

    /** Handles client's offline command by referencing args in TOKENS. */
    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    /** Handles client's online command by referencing args in TOKENS. */
    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
    }

    /** Returns true iff a connection is made. Otherwise return false. */
    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn =
                    new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException ex) {
            System.out.println("IOException");
        }
        return false;
    }

    /** Add a listener LISTENER to UserStatusListeners. */
    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    /** Remove a listener LISTENER to UserStatusListeners. */
    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    /** Add a listener LISTENER to MessageListeners. */
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    /** Remove a listener LISTENER to MessageListeners. */
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
}
