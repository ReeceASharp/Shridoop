package fileSystem.node.server;

import fileSystem.node.Heartbeat;
import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.protocols.events.*;
import fileSystem.transport.TCPReceiver;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

import static fileSystem.protocols.Protocol.*;

public class ChunkServer extends Node implements Heartbeat {
    private static final Logger logger = LogManager.getLogger(ChunkServer.class);

    //needed for console commands, not the most DRY
    final String[] commandList = {"list-files", "config"};
    private final int controllerPort;
    private final String name;

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
        Semaphore setupLock = new Semaphore(1);
        setupLock.tryAcquire();
        Thread tcpServer = new Thread(new TCPServer(server, portListen, setupLock));
        tcpServer.start();

        //create the console, this may not be needed for the chunkServer, but could be useful for debugging
        Thread console = new Thread(new ConsoleParser(server));
        console.start();

        try {
            setupLock.acquire();
            sendRegistration(server, host, portConnect);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Chunk Server wants to register with the Controller after setting up, throw a message at it to check
     *
     * @param node the ChunkServer sending the registration request
     * @param host The hostname/IP of the ChunkServer
     * @param port The port of the ChunkServer
     * @throws IOException thrown if the socket creation fails for some reason
     */
    private static void sendRegistration(ChunkServer node, String host, int port) throws IOException {
        logger.debug(String.format("SENDING REGISTRATION TO %s:%d", host, port));

        //open a socket/connection with the Controller, and set variables to be referenced later
        Socket controllerSocket = new Socket(host, port);

        //construct the message, and get the bytes
        byte[] marshalledBytes = new ChunkServerRequestsRegistration(node.getServerIP(),
                node.getServerPort(), node.getName()).getBytes();

        //create a listener on this new connection to listen for future requests/responses
        Thread receiver = new Thread(new TCPReceiver(node, controllerSocket, node.server));
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

    @Override
    public void onEvent(Event e, Socket socket) {
        switch (e.getType()) {
            // Controller -> ChunkServer
            case CONTROLLER_REPORTS_REGISTRATION_STATUS:
                registrationStatus(e);
                break;
            case CONTROLLER_REQUESTS_DEREGISTRATION:
                deregistration(e, socket);
                break;
            case CONTROLLER_REPORTS_SHUTDOWN:
                cleanup();
                break;
            case CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT:
                respondWithStatus(socket);
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

    /**
     * Simply responding with the current status of the ChunkServer. For the most part the status shouldn't be
     * needed until more features are implemented. In the future error-checking/corrupted file chunks could be a status,
     * but at the moment simply sending a response to the Controller means the ChunkServer is still alive
     *
     * @param socket
     */
    private void respondWithStatus(Socket socket) {
        byte[] marshalledBytes = new ChunkServerReportsFunctionalHeartbeat(RESPONSE_SUCCESS).getBytes();
        sendMessage(socket, marshalledBytes);
    }

    private void deregistration(Event e, Socket socket) {
        ControllerRequestsDeregistration request = (ControllerRequestsDeregistration) e;

        //TODO: look at ControllerRequestsDeregistration for future feature info

        logger.debug("Received Deregistration request");

        //respond
        byte[] marshalledBytes = new ChunkServerReportsDeregistrationStatus(RESPONSE_SUCCESS, getServerIP(),
                getServerPort(), getName()).getBytes();

        sendMessage(socket, marshalledBytes);
    }

    private void registrationStatus(Event e) {
        ControllerReportsRegistrationStatus response = (ControllerReportsRegistrationStatus) e;

        switch (response.getStatus()) {
            case RESPONSE_SUCCESS:
                logger.info("ChunkServer successfully registered and setup with Controller");
                break;
            case RESPONSE_FAILURE:
                logger.error("ChunkServer failed to register with Controller");
                break;
            default:
                logger.error("ERROR: incorrect message response type received");
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

    public String getName() {
        return name;
    }

    @Override
    public void cleanup() {
        logger.debug("EXITING CHUNKSERVER");

        server.cleanup();

        // Note: because the console has a Scanner waiting for an input from System.in,
        // the only real way to exit is via the System, which is fine considering the
        // server was shut down gracefully
        System.exit(0);
    }

    @Override
    public void onHeartBeat() {
        //send out a current summary of changes to the ChunkServer since the last heartbeat
    }
}
