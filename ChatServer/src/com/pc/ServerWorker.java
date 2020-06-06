package com.pc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.*;

/** Worker class for chat server. */
public class ServerWorker extends Thread {

    /** Socket for single client. */
    private final Socket clientSocket;

    /** Login name for this server worker. */
    private String login = null;

    /** The server this worker belongs to. */
    private Server server;

    /** Output stream of this worker. */
    private OutputStream outputStream;

    /** HashSet containing topics this worker belongs to. */
    private HashSet<String> topicSet = new HashSet<>();

    /** Constructor for ServerWorker, takes in socket CLIENTSOCKET and a
     * server SERVER. */
    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException ex) {
            System.out.println("IOException.");
        } catch (InterruptedException ex) {
            System.out.println("InterruptedException.");
        }
    }

    /** Reads text in from user using an input stream. */
    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("logoff".equals(cmd) || "exit".equalsIgnoreCase(cmd) ||
                        "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
                } else {
                    String msg = "Unknown command " + cmd + ".\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    /** Removes a user from a topic according to TOKENS. */
    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    /** Returns true iff this worker is a member of the topic TOPIC. */
    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    /** Adds user to a topic according to TOKENS. */
    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    /** Sends a message to another user. Takes in an array of strings TOKENS.
     *  Format: "msg" "login" body...
     *  Format: "msg" "#topic" body...*/
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];
        boolean isTopic = sendTo.charAt(0) == '#';
        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            } else if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                String outMsg = "msg " + login + " " + body + "\n";
                worker.send(outMsg);
            }
        }
    }

    /** Logs user out. */
    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();
        System.out.println("User " + login + " logged off.");
        // Send other online users current user's status.
        String onlineMsg = "offline " + login + "\n";
        for (ServerWorker worker : workerList) {
            if (worker != this) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    /** Returns the login of this worker. */
    public String getLogin() {
        return login;
    }

    /** Logs user in command. Takes in an outputStream OUTPUTSTREAM and an
     * array of tokens TOKENS. */
    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];
            if ((login.equals("guest") && password.equals("guest")) || (login.equals("brendan") && password.equals("benner"))) {
                String msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in succesfully: " + login);
                List<ServerWorker> workerList = server.getWorkerList();
                // Send current user all other online logins.
                for (ServerWorker worker : workerList) {
                    if (worker.getLogin() != null) {
                        if (worker != this) {
                            String workerLogin =
                                    "online " + worker.getLogin() + "\n";
                            send(workerLogin);
                        }
                    }
                }
                // Send other online users current user's status.
                String onlineMsg = "online " + login + "\n";
                for (ServerWorker worker : workerList) {
                    if (worker != this) {
                        worker.send(onlineMsg);
                    }
                }

            } else {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + login);
            }
        }
    }

    /** Accesses output stream of current client socket and sends a message
     * msg to the user. */
    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}
