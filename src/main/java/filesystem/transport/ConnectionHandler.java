package filesystem.transport;

import filesystem.interfaces.EventInterface;
import filesystem.interfaces.ServerInterface;
import filesystem.util.HostPortAddress;
import filesystem.util.NodeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Manages the connections between nodes, including reading and writing to the socket streams
 * Wraps the socket streams
 */
public class ConnectionHandler implements ServerInterface {
    private static final Logger logger = LogManager.getLogger(ConnectionHandler.class);

    private final EventInterface eventInterface;

    private final TCPServer server;
    //    TODO: Convert currentConnections and Connections to one datastructure
    private final ArrayList<SocketWrapper> connections;


    public ConnectionHandler(int listening_port, EventInterface eventInterface) {
        this.connections = new ArrayList<>();
        this.server = new TCPServer(listening_port, this);

        this.eventInterface = eventInterface;
    }

    /**
     * Connects to a node and returns the socket stream
     *
     * @param address the address of the node to connect to
     * @return the socket stream
     */
    public SocketWrapper connect(HostPortAddress address) {
        try {
            Socket connection = new Socket(address.getHostname(), address.getPort());
            SocketWrapper ss = new SocketWrapper(connection, eventInterface);
            addConnection(ss);

            return ss;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return NodeUtils.GenericListFormatter.getFormattedOutput(connections, "|", true);
    }

    public synchronized int size() {
        return connections.size();
    }

    public synchronized SocketWrapper addConnection(Socket socket) throws IOException {
        SocketWrapper ss = new SocketWrapper(socket, eventInterface);
        return addConnection(ss);
    }

    /**
     * Adds a connection to the list of connections
     *
     * @param ss the socket stream to add
     */
    public synchronized SocketWrapper addConnection(SocketWrapper ss) {
        if (connections.add(ss)) {
            return ss;
        } else {
            throw new RuntimeException("Failed to add connection: " + ss);
        }
    }

    public synchronized void removeConnection(Socket socket) {
        connections.remove(getSocketStream(socket));
    }

    public synchronized void removeConnection(SocketWrapper ss) {
        connections.remove(ss);
    }

    public synchronized SocketWrapper getSocketStream(Socket socket) {
        Optional<SocketWrapper> server = connections.stream().filter(socketStream -> socketStream.socket.equals(socket)).findFirst();
        return server.orElse(null);
    }

    public synchronized SocketWrapper getSocketStream(int index) {
        return connections.get(index);
    }

    public synchronized SocketWrapper getSocketStream(HostPortAddress address) {
        Optional<SocketWrapper> server = connections.stream().filter(socketStream -> socketStream.address.equals(address)).findFirst();
        return server.orElse(null);
    }

    public void cleanup() {
        this.server.cleanup();

        for (SocketWrapper connection : connections) {
            try {
                connection.cleanup();
            } catch (IOException e) {
                logger.error("Error cleaning up connection: " + connection.address);
            }
        }
        connections.clear();
        logger.debug("Cleanup has completed.");
    }

    @Override
    public boolean newServerConnection(Socket socket) {
        // Add it to the list to keep track of the connection

        try {
            return addConnection(new SocketWrapper(socket, eventInterface)) != null;
        } catch (IOException e) {
            logger.error("Error handling connection: " + socket);
            e.printStackTrace();
            return false;
        }
    }

    public int getServerPort() {
        return server.getServerPort();
    }

    public String getServerHost() {
        return server.getServerHost();
    }

}
