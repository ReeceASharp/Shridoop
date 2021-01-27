package fileSystem.transport;

import fileSystem.node.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

//TODO: Potentially refactor to have a variant for the Controller to utilize a threadpool instead

/**
 * Organizes the creation and listening of incoming connections
 */
public class TCPServer implements Runnable {
    private static final Logger logger = LogManager.getLogger(TCPServer.class);

    private final ArrayList<TCPReceiver> currentConnections;

    private final Node node;
    private final int port;
    private ServerSocket serverSocket;

    // Constructor, calls the main constructor with a port # of 0 will pick a random port
    public TCPServer(Node node) {
        this(node, 0);
    }

    // Constructor that is used if a certain port must be used for the server
    public TCPServer(Node node, int port) {
        this.node = node;
        this.port = port;

        currentConnections = new ArrayList<>();

        try {
            //create a socket on a pocket for incoming connections to connect to
            serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        logger.debug(String.format("[TCPSERVER] Port:%s, Socket:%s %n",
                port, serverSocket.getLocalSocketAddress().toString()));

        //set a reference after it has been fully constructed
        node.setTCPServer(this);

        try {
            while (true) {
                //block for incoming connections
                Socket clientSocket = serverSocket.accept();

                //handle new incoming connection

                new Thread(new TCPReceiver(node, clientSocket)).start();

            }
        } catch (SocketException e) {
            System.out.println("Exiting.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
        //close up the main listening port
        try {
            if (!serverSocket.isClosed())
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //close up the list of known connections, which will then be handled on the other side as well
        for (TCPReceiver connection : currentConnections) {
            connection.cleanup();
        }

        logger.debug("EXITING TCPSERVER");
    }

    public int getServerPort() {
        return port;
    }

    public String getServerIP() {
        return serverSocket.getInetAddress().getHostName();
    }

}
