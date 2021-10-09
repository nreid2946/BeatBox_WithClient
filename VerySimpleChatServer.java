package HeadFirstJava.Chapter15.AdviceServerAndClient.SimpleChatClient.Version3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class VerySimpleChatServer {

    ArrayList clientOutputStreams;

    public class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket socket;

        public ClientHandler (Socket clientSocket) {
            try {
                socket = clientSocket;
                System.out.println("Server: Got socket connection.");
                InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
                System.out.println("Server: socket.getInputStream() successfully executed.");
                reader = new BufferedReader(isReader);
                System.out.println("Server: Successfully chained.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } // close constructor.

        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("Read: " + message);
                    tellEveryone(message);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } // close run.
    }

    public static void main(String[] args) {
        VerySimpleChatServer chatserver = new VerySimpleChatServer();
        chatserver.go();
    }

    public void go() {
        clientOutputStreams = new ArrayList();
        try {
            ServerSocket serverSocket = new ServerSocket(4695);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (clientSocket.isConnected()) {
                    System.out.println("Server: clientSocket is connected.");
                }
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                clientOutputStreams.add(writer);

                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.start();
                System.out.println("Got a connection");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void tellEveryone(String message) {
        System.out.println("In tell everyone");
        Iterator it = clientOutputStreams.iterator();
        System.out.println("Iterating");
        while (it.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                System.out.println("writer.println(message)");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
