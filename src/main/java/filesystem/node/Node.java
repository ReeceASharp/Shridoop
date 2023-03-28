package filesystem.node;

import filesystem.interfaces.Command;
import filesystem.interfaces.Event;
import filesystem.interfaces.EventInterface;
import filesystem.pool.EventTask;
import filesystem.pool.PoolHandler;
import filesystem.transport.ConnectionHandler;
import filesystem.transport.SocketWrapper;
import filesystem.transport.TCPSender;
import filesystem.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstraction of a node, which each part of the system is build upon
 */
public abstract class Node implements EventInterface {
    //    TODO: REMOVE THIS
    public static final int CHUNK_SIZE = Properties.getInt("CHUNK_SIZE_BYTES");

    private static final Logger logger = LogManager.getLogger(Node.class);

    protected final ConnectionHandler connectionHandler;
    protected final Map<Integer, EventInterface> eventCallbacks;
    protected final PoolHandler poolHandler;


    public Node(int listening_port) {
        this.eventCallbacks = new HashMap<>();
        this.poolHandler = new PoolHandler();
        this.connectionHandler = new ConnectionHandler(listening_port, this);


        poolHandler.initializeWorkers(Properties.getInt("NODE_THREAD_POOL_WORKER_COUNT"));

        this.resolveEventMap();
    }

    /**
     * Callback for when an event is received by the connectionHandler
     *
     * @param event
     * @param socket
     */
    public void handleEvent(Event event, Socket socket) {
        try {
            EventInterface function = this.eventCallbacks.get(event.getType());
            poolHandler.addTask(new EventTask(event, socket, function));
        } catch (NullPointerException e) {
            logger.error(String.format("Unable to handle Event with # received: '%s', Event: %s", event.getType(), event));
            e.printStackTrace();
        }
    }

    /**
     * When receiving a command from a given TCP thread, do something with the request
     * public void onEvent(Event event, Socket socket) {
     * }
     * /**
     * send the bytes through a specific connection via thread
     *
     * @param socket The pipe through which data is being sent through
     * @param event  The message being sent (Uses objects)
     */
    protected void sendMessage(Socket socket, Event event) {
        sendMessage(connectionHandler.getSocketStream(socket), event);
    }

    protected void sendMessage(SocketWrapper ss, Event event) {
        logger.info(event + " -> " + ss);
        new Thread(new TCPSender(ss, event)).start();
    }

    /**
     * Used to get node information
     *
     * @return The hostname of the computer the node is running on
     */
    protected String hostname() {
        try {
//            TODO: Determine if there's a better way to do this. Maybe pass it in on init
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addConnection(SocketWrapper ss) {
        connectionHandler.addConnection(ss);
    }

    public String connectionInfo() {
        return connectionHandler.toString();
    }


    /**
     * Called by the consoleParser to quit gracefully
     */
    public abstract void cleanup();

    public abstract void onLostConnection(Socket socket);

    protected int getServerPort() {
        return connectionHandler.getServerPort();
    }

    protected String getServerHost() {
        return connectionHandler.getServerHost();
    }

    /**
     * A mapping of commands to their resulting commands on the node. Combined with a set of default console commands
     * determined by the ConsoleParser to give a customized set of commands for each type of node.
     */
    public abstract Map<String, Command> getCommandList();

    /**
     * Adds functions to the eventActions map that will allow the ConsoleParser to call on user input
     */
    protected abstract void resolveEventMap();
}