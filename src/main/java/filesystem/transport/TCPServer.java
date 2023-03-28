package filesystem.transport;

import filesystem.interfaces.ServerInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Organizes the creation and listening of incoming connections
 */
public class TCPServer implements Runnable {
    private static final Logger logger = LogManager.getLogger(TCPServer.class);

    private final int listening_port;
    private ServerSocket serverSocket;

    // Callback for when a new connection is received
    private final ServerInterface serverInterface;

    public TCPServer(int listening_port, ServerInterface serverInterface) {
        this.listening_port = listening_port;
        this.serverInterface = serverInterface;

        try {
            serverSocket = new ServerSocket(listening_port);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "TCPServer{" +
                ", port=" + listening_port +
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
                boolean handled = serverInterface.newServerConnection(incomingSocket);

                if (!handled) {
                    throw new IOException("TCPServer was not handled. Closing TCPServer.");
                }
            }
        } catch (SocketException e) {
            logger.debug("Socket closed. " + e.getMessage());
        } catch (IOException e) {
            logger.error("Received an unhandled exception. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cleanup() {
        try {
            if (!serverSocket.isClosed())
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getServerPort() {
        return listening_port;
    }

    public String getServerHost() {
        return serverSocket.getInetAddress().getHostName();
    }

}
