package filesystem.transport;

import filesystem.node.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Organizes the creation and listening of incoming connections
 */
public class TCPServer implements Runnable {
    private static final Logger logger = LogManager.getLogger(TCPServer.class);

    private final ArrayList<TCPReceiver> currentConnections;
    private final Node node;
    private final int port;
    private ServerSocket serverSocket;

    public TCPServer(Node node, int port) {
        this.node = node;
        this.port = port;

        currentConnections = new ArrayList<>();

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "TCPServer{" +
                "currentConnections=" + currentConnections +
                ", port=" + port +
                ", serverSocket=" + serverSocket +
                '}';
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());

        logger.debug(String.format("Accepting Connections on: Port:%s, Socket:%s",
                serverSocket.getLocalPort(),
                serverSocket.getLocalSocketAddress().toString()));

        try {
            // Accept new connections
            while (true) {
                Socket incomingSocket = serverSocket.accept();
                SocketStream ss = new SocketStream(incomingSocket);
                node.connectionMetadata.addConnection(ss);
                new Thread(new TCPReceiver(node, ss, this)).start();
            }
        } catch (SocketException e) {
            System.out.println("Exiting.");
        } catch (IOException e) {
            logger.error("Received an unhandled exception." + e.getMessage());
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
        currentConnections.clear();

        logger.debug("Cleanup has completed.");
    }

    public int getServerPort() {
        return port;
    }

    public String getServerHost() {
        return serverSocket.getInetAddress().getHostName();
    }

    public void addConnection(Node hostNode, SocketStream ss) {
        TCPReceiver connection = new TCPReceiver(hostNode, ss, this);
        currentConnections.add(connection);
        new Thread(connection).start();
    }

    public void removeConnection(TCPReceiver connection) {
        currentConnections.remove(connection);

    }

}
