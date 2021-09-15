package filesystem.node;

import filesystem.protocol.*;
import filesystem.transport.*;
import filesystem.util.*;
import filesystem.util.Properties;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Abstraction of a node, which each part of the system is build upon
 */
public abstract class Node {
    public static final int CHUNK_SIZE = Integer.parseInt(Properties.get("CHUNK_SIZE"));

    public Node() {
        this.connectionMetadata = new ConnectionMetadata();
        this.eventActions = new HashMap<>();

        this.resolveEventMap();
    }


    // Cluster connections
    public final ConnectionMetadata connectionMetadata;
    protected TCPServer server;
    protected ConsoleParser console;
    protected final Map<Integer, EventAction> eventActions;

    protected abstract void resolveEventMap();

    protected SocketStream connect(String host, int port) {
        try {
            Socket connection = new Socket(host, port);
            SocketStream ss = new SocketStream(connection);
            this.connectionMetadata.addConnection(ss);

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
        sendMessage(connectionMetadata.getSocketStream(socket), event);
    }

    protected void sendMessage(SocketStream ss, Event event) {
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
            e.printStackTrace();
        }
    }

    /**
     * Called by the consoleParser to quit gracefully
     */
    public abstract void cleanup();

    public abstract void onLostConnection(Socket socket);

    /**
     * Set a reference inside of the node to the relating TCPServer. Benefit is it allows a graceful exit, and
     * can be used to get config information more easily, instead of it being stored redundantly inside the node
     *
     * @param ref a reference to the TCPServer
     */
    public void setTCPServer(TCPServer ref) {
        this.server = ref;
    }

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

    /**
     * Caches the state for node to a configurable location. Works with updateFromCache().This allows
     * the cluster to be brought up and down successfully.
     */
    protected abstract void cacheInfo();

    /**
     * Uses the cached information from cacheInfo() to set the state of the node.
     */
    protected abstract void updateFromCache();


    protected interface EventAction {
        void runAction(Event e, Socket socket);
    }



}