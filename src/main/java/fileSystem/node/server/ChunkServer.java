package fileSystem.node.server;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.protocols.events.ChunkServerSendsRegistration;
import fileSystem.transport.TCPReceiver;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static fileSystem.protocols.Protocol.*;

public class ChunkServer extends Node {
    private static final Logger logger = LogManager.getLogger(ChunkServer.class);

    //needed to display, not the most DRY
    final String[] commandList = {"list-files", "config"};
    private final int controllerPort;
    private final String name;

    private Socket controllerSocket;

    public ChunkServer(int portConnect, String name) {
        this.controllerPort = portConnect;
        this.name = name;
    }

    public static void main(String[] args) throws UnknownHostException {
        final int portListen = Integer.parseInt(args[0]);
        final int portConnect = Integer.parseInt(args[1]);
        final String name = args[2];

        logger.debug(String.format("PortListen: %d, PortConnect: %d, Name: %s", portListen, portConnect, name));

        //random port
        //int port = 0;

        //get the hostname and IP address
        InetAddress ip = InetAddress.getLocalHost();
        String host = ip.getHostName();
        logger.debug(String.format("IP: %s, Host: %s%n", ip.getHostAddress(), host));

        //get an object reference to be able to call functions and organize control flow
        ChunkServer server = new ChunkServer(portConnect, name);

        //create a server thread to listen to incoming connections
        Thread tcpServer = new Thread(new TCPServer(server, portListen));
        tcpServer.start();

        //create the console, this may not be needed for the chunkServer, but could be useful for debugging
        Thread console = new Thread(new ConsoleParser(server));
        console.start();

        try {
            sendRegistration(server, host, portConnect);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Chunk Server wants to register with the Controller, throw a message at it to check
     *
     * @param node the ChunkServer sending the registration request
     * @param host The hostname/IP of the ChunkServer
     * @param port The port of the ChunkServer
     * @throws IOException
     */
    private static void sendRegistration(ChunkServer node, String host, int port) throws IOException {
        logger.debug(String.format("SENDING REGISTRATION TO %s:%d", host, port));

        //open a socket/connection with the Controller, and set variables to be referenced later
        Socket controllerSocket = new Socket(host, port);
        node.setControllerSocket(controllerSocket);

        //construct the message, and get the bytes
        byte[] marshalledBytes = new ChunkServerSendsRegistration(node.getServerIP(),
                node.getServerPort(), node.getName()).getBytes();

        //create a listener on this socket for the response from the Registry
        Thread receiver = new Thread(new TCPReceiver(node, controllerSocket));
        receiver.start();

        //Send the message to the Registry to attempt registration
        node.sendMessage(controllerSocket, marshalledBytes);
    }

    /**
     * Print out the details of the ChunkServer in a formatted way
     */
    private void showConfig() {
        System.out.printf("ServerName: '%s', ControllerPort: '%s'%n", name, controllerPort);
    }

    private void setControllerSocket(Socket controllerSocket) {
        this.controllerSocket = controllerSocket;
    }

    @Override
    public void onEvent(Event e, Socket socket) {
        switch (e.getType()) {
            // Controller -> ChunkServer
            case CONTROLLER_REPORTS_REGISTRATION_STATUS:
                break;
            case CONTROLLER_REQUESTS_MAJOR_HEARTBEAT:
                break;
            case CONTROLLER_REQUESTS_MINOR_HEARTBEAT:
                break;
            case CONTROLLER_REQUESTS_FILE_METADATA:
                break;

            // ChunkServer -> ChunkServer
            case CHUNK_SERVER_REQUESTS_REPLICATION:
                break;
            case CHUNK_SERVER_REPORTS_REPLICATION:
                break;

            // Client -> ChunkServer
            case CLIENT_REQUESTS_FILE_CHUNK:
                break;
        }
    }

    @Override
    public boolean handleCommand(String input) {
        boolean isValid = true;
        switch (input) {
            case "list-files":
                break;
            case "config":
                showConfig();
                break;
            default:
                isValid = false;
        }
        return isValid;
    }

    @Override
    protected String getHelp() {
        return "This is strictly used for development and to see system details locally. " +
                "Available commands are shown with 'commands'.";
    }

    @Override
    protected String getIntro() {
        return "Distributed System ChunkServer (DEV ONLY), type " +
                "'help' for more details: ";
    }

    @Override
    public String[] getCommands() {
        return commandList;
    }

    public String getName() { return name; }

    @Override
    public void cleanup() {
        server.cleanup();
        logger.debug("EXITING CHUNKSERVER");
    }
}
