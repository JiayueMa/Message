package com.jiayuema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class MessageClientGUI {
    private static final String CONNECTION_SECRET_KEY = "your-secret-key"; // Define the connection secret key
    private String username;
    private String encryptionKey;
    private BufferedReader reader;
    private PrintWriter writer;

    public MessageClientGUI(String username, String encryptionKey) {
        this.username = username;
        this.encryptionKey = encryptionKey;
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("MessageClient - " + username);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        frame.add(new JScrollPane(textArea), "Center");

        JTextField textField = new JTextField();
        frame.add(textField, "South");

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                String encryptedMessage = encryptMessage(username + ": " + message);
                writer.println(encryptedMessage);
                textArea.append("You: " + message + " (Sent successfully)\n");
                textField.setText("");
            }
        });

        frame.setVisible(true);

        String hostname = "3.149.254.204"; // Replace with your EC2 public IP address
        int port = 8080;

        try {
            Socket socket = new Socket(hostname, port);
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            // Send the connection secret key
            writer.println(CONNECTION_SECRET_KEY);

            // Send the username
            writer.println(username);

            new Thread(() -> {
                String response;
                try {
                    while ((response = reader.readLine()) != null) {
                        System.out.println("Received message: " + response);
                        if (response.startsWith("ENCRYPTED:")) {
                            String decryptedMessage = decryptMessage(response.substring(10));
                            textArea.append(decryptedMessage + "\n");
                        } else {
                            textArea.append(response + "\n");
                        }
                    }
                } catch (IOException e) {
                    textArea.append("Server connection lost: " + e.getMessage() + "\n");
                }
            }).start();

        } catch (UnknownHostException ex) {
            textArea.append("Server not found: " + ex.getMessage() + "\n");
        } catch (IOException ex) {
            textArea.append("I/O error: " + ex.getMessage() + "\n");
        }
    }

    private String encryptMessage(String message) {
        try {
            String encrypted = AESUtil.encrypt(message, encryptionKey);
            System.out.println("Encrypted message: " + encrypted);
            return encrypted;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String decryptMessage(String message) {
        try {
            String decrypted = AESUtil.decrypt(message, encryptionKey);
            System.out.println("Decrypted message: " + decrypted);
            return decrypted;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel(new GridLayout(0, 1));
            JTextField usernameField = new JTextField();
            JPasswordField keyField = new JPasswordField();
            panel.add(new JLabel("Enter your username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Enter the encryption key:"));
            panel.add(keyField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String username = usernameField.getText();
                String encryptionKey = new String(keyField.getPassword());
                if (username != null && !username.trim().isEmpty() && encryptionKey != null && !encryptionKey.trim().isEmpty()) {
                    new MessageClientGUI(username, encryptionKey);
                } else {
                    JOptionPane.showMessageDialog(null, "Username and encryption key must not be empty", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
