package filesystem.node;

import filesystem.pool.Command;
import filesystem.pool.EventAction;
import filesystem.pool.EventTask;
import filesystem.pool.PoolHandler;
import filesystem.protocol.Event;
import filesystem.transport.ConnectionHandler;
import filesystem.transport.SocketStream;
import filesystem.transport.TCPSender;
import filesystem.transport.TCPServer;
import filesystem.console.ConsoleParser;
import filesystem.util.HostPortAddress;
import filesystem.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstraction of a node, which each part of the system is build upon
 */
public abstract class Node {
    public static final int CHUNK_SIZE = Properties.getInt("CHUNK_SIZE_BYTES");
    private static final Logger logger = LogManager.getLogger(Node.class);

    protected final ConnectionHandler connectionHandler;
    protected final Map<Integer, EventAction> eventFunctions;
    protected final PoolHandler poolHandler;
    protected TCPServer server;
    protected ConsoleParser console;

    public Node() {
        this.connectionHandler = new ConnectionHandler();
        this.eventFunctions = new HashMap<>();
        this.poolHandler = new PoolHandler();

        poolHandler.initializeWorkers(Properties.getInt("NODE_THREAD_POOL_WORKER_COUNT"));

        this.resolveEventMap();
    }

    protected String generateID() {
        return null;
    }


    protected SocketStream connect(HostPortAddress address) {
        try {
            Socket connection = new Socket(address.getHostname(), address.getPort());
            SocketStream ss = new SocketStream(connection);
            this.connectionHandler.addConnection(ss);
            this.server.addConnection(this, ss);

            return ss;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * send the bytes through a specific connection via thread
     *
     * @param socket The pipe through which data is being sent through
     * @param event  The message being sent (Uses objects)
     */
    protected void sendMessage(Socket socket, Event event) {
        sendMessage(connectionHandler.getSocketStream(socket), event);
    }

    protected void sendMessage(SocketStream ss, Event event) {
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

    /**
     * information displayed on console help request
     *
     * @return a string containing instructions on how to use the program
     */
    public abstract String help();

    public void addConnection(SocketStream ss) {
        connectionHandler.addConnection(ss);
    }

    public String connectionInfo() {
        return connectionHandler.toString();
    }

    /**
     * information displayed on startup
     *
     * @return a string containing a basic description of the node
     */
    public abstract String intro();

    /**
     * When receiving a command from a given TCP thread, do something with the request
     *
     * @param event  event to be decoded and handled
     * @param socket pipeline that the event came on in
     */
    public void onEvent(Event event, Socket socket) {
        try {
            EventAction function = this.eventFunctions.get(event.getType());
            poolHandler.addTask(new EventTask(event, socket, function));
        } catch (NullPointerException e) {
            logger.error(String.format("Unable to handle Event with # received: '%s', Event: %s",
                    event.getType(), event));
            e.printStackTrace();
        }
    }

    /**
     * Called by the consoleParser to quit gracefully
     */
    public abstract void cleanup();

    public abstract void onLostConnection(Socket socket);

    protected int getServerPort() {
        return server.getServerPort();
    }

    protected String getServerHost() {
        return server.getServerHost();
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