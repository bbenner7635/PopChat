package com.pc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/** Login window GUI. */
public class LoginWindow extends JFrame {

    /** Chat client of this login window. */
    private final ChatClient client;

    /** Text field for the login. */
    JTextField loginField = new JTextField();

    /** Text field for the password. */
    JTextField passwordField = new JTextField();

    /** Button to login. */
    JButton loginButton = new JButton("Login");

    /** Constructor for the login window. */
    public LoginWindow () {
        super("Login");
        this.client = new ChatClient("localhost", 7635);
        client.connect();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(loginButton);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });
        getContentPane().add(p, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    /* Logs in client with provided details. */
    private void doLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();
        try {
            if (client.login(login, password)) {
                // bring up the user list window
                UserListPane userListPane = new UserListPane(client);
                JFrame frame = new JFrame("User List");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 600);
                frame.getContentPane().add(userListPane,
                        BorderLayout.CENTER);
                frame.setVisible(true);
                setVisible(false);
            } else {
                // show error message
                JOptionPane.showMessageDialog(this, "Invalid login/password.");
            }
        } catch (IOException ex) {
            System.out.println("IOException.");
        }
    }

    /** Creates a login window, takes in array of strings ARGS. */
    public static void main(String[] args) {
        LoginWindow loginWin = new LoginWindow();
        loginWin.setVisible(true);
    }

}
