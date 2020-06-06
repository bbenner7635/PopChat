package com.pc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/** Message pane GUI. */
public class MessagePane extends JPanel implements MessageListener {

    /** The chat client of this message pane. */
    private final ChatClient client;

    /** The login of the client of this message pane. */
    private final String login;

    /** List model for JList. */
    private DefaultListModel<String> listModel = new DefaultListModel<>();

    /** List of current conversations. */
    private JList<String> messageList = new JList<>(listModel);

    /** Text field for messages. */
    private JTextField inputField = new JTextField();

    /** Constructor for the message pane that takes in a client CLIENT and
     * login LOGIN. */
    public MessagePane(ChatClient client, String login) {
        this.client = client;
        this.login = login;
        client.addMessageListener(this);
        setLayout(new BorderLayout());
        add (new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = inputField.getText();
                    client.msg(login, text);
                    listModel.addElement("You: " + text);
                    inputField.setText("");
                } catch (IOException ex) {
                    System.out.println("IOException.");
                }
            }
        });
    }

    @Override
    public void onMessage(String fromLogin, String msgBody) {
        System.out.println("messaged");
        if (login.equalsIgnoreCase(fromLogin)) {
            String line = fromLogin + ": " + msgBody;
            listModel.addElement(line);
        }
    }
}
