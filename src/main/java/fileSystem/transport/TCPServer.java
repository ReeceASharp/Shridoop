package fileSystem.transport;

import fileSystem.node.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * Organizes the creation and listening of incoming connections
 */
public class TCPServer implements Runnable {
    private static final Logger logger = LogManager.getLogger(TCPServer.class);

    private final ArrayList<TCPReceiver> currentConnections;

    private final Node node;
    private final int port;
    private final Semaphore setupLock;
    private ServerSocket serverSocket;

    // Constructor that is used if a certain port must be used for the server
    public TCPServer(Node node, int port, Semaphore setupLock) {
        this.node = node;
        this.port = port;
        this.setupLock = setupLock;

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
        Thread.currentThread().setName(getClass().getSimpleName());

        logger.debug(String.format("Port:%s, Socket:%s %n",
                port, serverSocket.getLocalSocketAddress().toString()));

        //set a reference after it has been fully constructed, used for cleanup
        node.setTCPServer(this);

        try {
            // Note: this is used when sending a connection request out. It requires a lock
            // to ensure the server has completed its configuration before allowing other threads to grab
            // information from it to send away, but it is only used if a setup lock was pass in
            if (setupLock != null)
                setupLock.release();

            while (true) {
                //block for incoming connections
                Socket incomingSocket = serverSocket.accept();
                //handle new incoming connection
                SocketStream ss = new SocketStream(incomingSocket);
                node.connectionHandler.addConnection(ss);

                new Thread(new TCPReceiver(node, ss, this)).start();

                //logger.debug(node.connectionHandler);
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
        currentConnections.clear();

        logger.debug("EXITING TCPSERVER");
    }

    public int getServerPort() {
        return port;
    }

    public String getServerIP() {
        return serverSocket.getInetAddress().getHostName();
    }

    /**
     * Adds a reference to the known connections the server has
     *
     * @param connection a
     */
    public void addConnection(TCPReceiver connection) {
        currentConnections.add(connection);
    }

    public void removeConnection(TCPReceiver connection) {
        currentConnections.remove(connection);
    }

}
