package filesystem.node;

import filesystem.protocol.Event;
import filesystem.transport.ConnectionHandler;
import filesystem.transport.SocketStream;
import filesystem.transport.TCPSender;
import filesystem.transport.TCPServer;
import filesystem.util.Command;
import filesystem.util.ConsoleParser;
import filesystem.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstraction of a node, which each part of the system is build upon
 */
public abstract class Node {
    private static final Logger logger = LogManager.getLogger(Node.class);

    public static final int CHUNK_SIZE = Integer.parseInt(Properties.get("CHUNK_SIZE_BYTES"));

    public Node() {
        this.connectionHandler = new ConnectionHandler();
        this.eventActions = new HashMap<>();

        this.resolveEventMap();
    }


    // Cluster connections
    public final ConnectionHandler connectionHandler;
    protected TCPServer server;
    protected ConsoleParser console;
    protected final Map<Integer, EventAction> eventActions;

    protected abstract void resolveEventMap();




    protected SocketStream connect(InetSocketAddress address) {
        try {
            Socket connection = new Socket(address.getAddress(), address.getPort());
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
            return InetAddress.getLocalHost().getHostName();
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
            EventAction action = this.eventActions.get(event.getType());
            action.runAction(event, socket);
        } catch (NullPointerException e) {
            logger.error(String.format("Unable to handle Event with # received: '%s', Event: %s",
                    event.getType(), event.toString()));
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
     *
     * @return
     */
    public abstract Map<String, Command> getCommandList();


    protected interface EventAction {
        void runAction(Event e, Socket socket);
    }



}