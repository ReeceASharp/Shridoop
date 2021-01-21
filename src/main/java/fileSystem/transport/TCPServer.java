package fileSystem.transport;

import fileSystem.node.Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPServer implements Runnable {
    private ServerSocket serverSocket;
    private final Node node;
    private final int port;

    // Constructor, calls the main constructor with a port # of 0 will pick a random port
    public TCPServer(Node node) {
        this(node, 0);
    }

    // Constructor that is used if a certain port must be used for the server
    public TCPServer(Node node, int port) {
        this.node = node;
        this.port = port;

        try {
            //create a socket on a pocket for incoming connections to connect to
            serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        System.out.printf("[TCPSERVER] Port:%s, Socket:%s %n", port, serverSocket.getLocalSocketAddress().toString());

        try {
            while (true) {
                //block for incoming connections
                Socket clientSocket = serverSocket.accept();
                //handle new incoming connection
                new Thread(new TCPReceiver(node, clientSocket)).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
