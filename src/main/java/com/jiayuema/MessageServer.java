import java.io.*;
import java.net.*;
import java.util.*;

public class MessageServer {
    private static final String CONNECTION_SECRET_KEY = "your-secret-key"; // Define the connection secret key
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    private static Map<Socket, String> clientUsernames = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server is listening on port 8080");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new ServerThread(socket).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    static class ServerThread extends Thread {
        private Socket socket;
        private PrintWriter writer;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                writer = new PrintWriter(output, true);

                // Validate the connection secret key
                String receivedKey = reader.readLine();
                if (!CONNECTION_SECRET_KEY.equals(receivedKey)) {
                    socket.close();
                    System.out.println("Invalid client connection closed");
                    return;
                }

                // Read the username
                String username = reader.readLine();
                synchronized (clientWriters) {
                    clientWriters.add(writer);
                    clientUsernames.put(socket, username);
                }

                broadcastMessage(username + " has joined the chat.", null);

                String text;
                while ((text = reader.readLine()) != null) {
                    System.out.println("Received from " + username + ": " + text); // Log encrypted message with username
                    broadcastMessage("ENCRYPTED:" + text, writer);
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(writer);
                    String username = clientUsernames.remove(socket);
                    if (username != null) {
                        broadcastMessage(username + " has left the chat.", null);
                    }
                }
            }
        }
    }

    private static void broadcastMessage(String message, PrintWriter excludeWriter) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                if (writer != excludeWriter) {
                    writer.println(message);
                }
            }
        }
    }
}
