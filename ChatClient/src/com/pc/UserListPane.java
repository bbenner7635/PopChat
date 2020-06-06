package com.pc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/** GUI user pane. */
public class UserListPane extends JPanel implements UserStatusListener {

    /** Client for this User List Pane. */
    private final ChatClient client;

    /** JList of users. */
    private JList<String> userListUI;

    /** Model for the user list. */
    private DefaultListModel<String> userListModel;

    /** Constructor for this User List Pane. Takes in a client CLIENT. */
    public UserListPane(ChatClient client) {
        this.client = client;
        client.addUserStatusListener(this);
        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);
        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    String login = userListUI.getSelectedValue();
                    MessagePane messagePane = new MessagePane(client, login);
                    JFrame f = new JFrame("Message: " + login);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500, 500);
                    f.getContentPane().add(messagePane, BorderLayout.CENTER);
                    f.setVisible(true);
                }
            }
        });
    }

    /** Creates a frame and makes it visible, takes an array of strings ARGS. */
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 7635);
        UserListPane userListPane = new UserListPane(client);
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        frame.getContentPane().add(userListPane,
                BorderLayout.CENTER);
        frame.setVisible(true);
        if (client.connect()) {
            try {
                client.login("guest", "guest");
            } catch (IOException ex) {
                client.logoff();
                System.out.println("IOException.");
            }
        }
    }

    @Override
    public void online(String login) {
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }
}
