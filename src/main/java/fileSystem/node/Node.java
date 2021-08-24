package fileSystem.node;

import fileSystem.protocol.Event;
import fileSystem.transport.ConnectionHandler;
import fileSystem.transport.SocketStream;
import fileSystem.transport.TCPSender;
import fileSystem.transport.TCPServer;
import fileSystem.util.Command;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

import static fileSystem.util.ConsoleParser.*;

/**
 * Abstraction of a node, which each part of the system is build upon
 */
public abstract class Node {
    public static final int CHUNK_SIZE = 65536;
    //Connection handler that generates the input/output streams for easy access/reusability
    public final ConnectionHandler connectionHandler = new ConnectionHandler();
    //reference to the server
    protected TCPServer server;

    /**
     * send the bytes through a specific connection via thread
     *
     * @param socket The pipe through which data is being sent through
     * @param event  The message being sent (Uses objects)
     */
    protected void sendMessage(Socket socket, Event event) {
        //convert socket to socketstream
        SocketStream ss = connectionHandler.getSocketStream(socket);
        //System.out.println("SENDING MESSAGE: " + ss);
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
     * @param e      event to be decoded and handled
     * @param socket pipeline that the event came on in
     */
    public abstract void onEvent(Event e, Socket socket);

    /**
     * Called by the consoleParser to quit gracefully
     */
    public abstract void cleanup();

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

    public abstract Map<String, Command> getCommandMap();

}
