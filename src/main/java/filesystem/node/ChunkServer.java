package filesystem.node;

import filesystem.protocol.Event;
import filesystem.protocol.events.*;
import filesystem.transport.SocketStream;
import filesystem.transport.TCPReceiver;
import filesystem.transport.TCPServer;
import filesystem.util.*;
import filesystem.util.metadata.FileChunkData;
import filesystem.util.taskscheduler.TaskScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static filesystem.protocol.Protocol.*;
import static filesystem.util.Utils.appendLn;

public class ChunkServer extends Node implements HeartBeat {


    private static final Logger logger = LogManager.getLogger(ChunkServer.class);

    //needed for console commands, not the most DRY
    private final String serverName;
    private final String homePath;
    private final FileHandler fileHandler;
    private TaskScheduler timer;

    public ChunkServer(String serverName, String homePath) {
        super();

        this.serverName = serverName;
        this.homePath = homePath;
        this.fileHandler = new FileHandler(homePath);
    }

    public static void main(String[] args) throws IOException {
        // Parse commandline args
        final String controllerHost = args[0];
        final int controllerPort = Integer.parseInt(args[1]);
        final int listenPort = Integer.parseInt(args[2]);
        final String serverName = args[3];
        final String homePath = args[4];

        ChunkServer server = new ChunkServer(serverName, homePath);
        server.setup(controllerHost, controllerPort, listenPort);

        logger.debug(String.format("Listen: %d, serverName: %s, StoragePath: %s", listenPort, serverName, homePath));
    }

    private void setup(String controllerHost, int controllerPort, int listenPort) throws IOException {
        Files.createDirectories(Paths.get(this.homePath));

        this.timer = new TaskScheduler(this.getClass().getName());
        this.server = new TCPServer(this, listenPort);
        this.console = new ConsoleParser(this);

        new Thread(this.server).start();
        new Thread(this.console).start();

        this.sendRegistration(controllerHost, controllerPort);
    }

    /**
     * Chunk Server wants to register with the Controller after setting up, throw a message at it to check
     *
     * @param controllerHost The hostname/IP of the ChunkServer
     * @param controllerPort The port of the ChunkServer
     * @throws IOException thrown if the socket creation fails for some reason
     */
    private void sendRegistration(String controllerHost, int controllerPort) throws IOException {
        //logger.debug(String.format("SENDING REGISTRATION TO %s:%d", host, port));

        //construct the message, and get the bytes
        Event e = new ChunkServerRequestsRegistration(this.getServerName(),
                this.getServerHost(),
                this.getServerPort());

        //open a socket/connection with the Controller, and set variables to be referenced later
        Socket controllerSocket = new Socket(controllerHost, controllerPort);

        SocketStream ss = new SocketStream(controllerSocket);
        this.connectionMetadata.addConnection(ss);
        //create a listener on this new connection to listen for future requests/responses
        Thread receiver = new Thread(new TCPReceiver(this, ss, this.server));
        receiver.start();

        //Send the message to the Registry to attempt registration
        //logger.debug(node.connectionHandler);
        this.sendMessage(controllerSocket, e);
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    protected void resolveEventMap() {
        this.eventActions.put(CONTROLLER_REPORTS_REGISTRATION_STATUS, this::registrationStatus);
        this.eventActions.put(CONTROLLER_REQUESTS_DEREGISTRATION, this::deregistration);
        this.eventActions.put(CONTROLLER_REPORTS_SHUTDOWN, this::handleShutdown);
        this.eventActions.put(CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT, this::respondWithStatus);
        //this.eventActions.put(CHUNK_SERVER_REQUESTS_REPLICATION, this::respondWithStatus);
        //this.eventActions.put(CHUNK_SERVER_REPORTS_REPLICATION, this::respondWithStatus);
        this.eventActions.put(CLIENT_REQUESTS_FILE_CHUNK, this::sendFileChunk);
        this.eventActions.put(NODE_SENDS_FILE_CHUNK, this::fileAdd);
    }

    @Override
    public String help() {
        return "This is strictly used for development and to see system details locally. " +
                       "Available commands are shown with 'commands'.";
    }

    @Override
    public String intro() {
        return "Distributed System ChunkServer (DEV ONLY), type " +
                       "'help' for more details: ";
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
    public void onLostConnection(Socket socket) {
        //IGNORE
    }

    @Override
    public Map<String, Command> getCommandList() {
        Map<String, Command> commandMap = new HashMap<>();

        commandMap.put("list-files", userInput -> listChunks());
        commandMap.put("config", userInput -> showConfig());

        return commandMap;
    }

    private String listChunks() {
        StringBuilder sb = new StringBuilder();
        appendLn(sb, "Home path: " + homePath);
        appendLn(sb, "***********************");
        for (FileChunkData smd : fileHandler.getFileChunks())
            appendLn(sb, smd.toString());
        appendLn(sb, "***********************");
        return sb.toString();
    }

    /**
     * Print out the details of the ChunkServer in a formatted way
     */
    private String showConfig() {
        return String.format("ServerName: '%s', " +
                                     "Path: '%s'%n" +
                                     "Server%s%n",
                serverName, homePath, server);
    }

    @Override
    protected void cacheInfo() {

    }

    @Override
    protected void updateFromCache() {

    }

    private void registrationStatus(Event e, Socket socket) {
        ControllerReportsRegistrationStatus response = (ControllerReportsRegistrationStatus) e;

        switch (response.getStatus()) {
            case RESPONSE_SUCCESS:
                logger.info("Successfully registered with Controller!");
                break;
            case RESPONSE_FAILURE:
                logger.error("Failed to register with Controller.");
                break;
            default:
                logger.error("ERROR: Incorrect message response type received");
        }

    }

    private void deregistration(Event e, Socket socket) {
        ControllerRequestsDeregistration request = (ControllerRequestsDeregistration) e;

        //TODO: look at ControllerRequestsDeregistration for future feature info

        //logger.debug("Received Deregistration request");

        //respond
        Event event = new ChunkServerReportsDeregistrationStatus(RESPONSE_SUCCESS, getServerHost(),
                getServerPort(), serverName);

        sendMessage(socket, event);
    }

    private void handleShutdown(Event e, Socket socket) {
        // Wrapping in a EventAction so that it can be called when the server controller requests a shutdown
        cleanup();
    }

    /**
     * Simply responding with the current status of the ChunkServer. For the most part the status shouldn't be
     * needed until more features are implemented. In the future error-checking/corrupted file chunks could be a status,
     * but at the moment simply sending a response to the Controller means the ChunkServer is still alive
     *
     * @param socket
     */
    private void respondWithStatus(Event e, Socket socket) {
        Event event = new ChunkServerReportsHeartbeat(RESPONSE_SUCCESS);
        sendMessage(socket, event);
    }

    /**
     * Respond to the fileRequest from the client
     *
     * @param e
     * @param socket
     */
    private void sendFileChunk(Event e, Socket socket) {
        ClientRequestsFileChunk request = (ClientRequestsFileChunk) e;

        // byte[] fileData = fileHandler.getFileData(request.getFile());
        //Event event = new ChunkServerSendsFileChunk(fileData);


        //sendMessage(socket, event);
    }

    /**
     * Handles the adding of a file, called either from a Client node, or from another ChunkServer
     * requesting a replication
     *
     * @param e      Base event passed in, is actually NodeSendsFileChunk with respective request values
     * @param socket The socket the event came in on
     */
    private void fileAdd(Event e, Socket socket) {
        NodeSendsFileChunk request = (NodeSendsFileChunk) e;

        logger.debug(String.format("Received new chunk: %s, %d bytes", request.getFileName(),
                request.getChunkData().length));

        String fileName = request.getFileName() + request.getChunkNumber();

        if (!request.getHash().equals(FileChunker.getChunkHash(request.getChunkData()))) {
            logger.error("Data does match origin. Requesting a new chunk.");
            //TODO: possibly exit method early and send a request back to the node for another file
            // in which case this will be started again
        }


        //store the file in the local directory for the ChunkServer
        fileHandler.storeFileChunk(fileName, request.getChunkData(), request.getHash());

        //Store the update in

        //update the contact details
        ArrayList<Pair<String, Integer>> serversToContact = request.getServersToContact();
        if (serversToContact.isEmpty()) {
            logger.debug("Completed Replication of file chunk: " + fileName);
            return;
        }

        //TODO: Fetch the full address of the next ChunkServer, not just the port. This means the request should contain
        // a datastructure that holds more metadata
        Pair<String, Integer> hostPort = serversToContact.remove(0);

        // Check if there is already a connection
        SocketStream socketStream = connectionMetadata.getSocketStream(hostPort.getLeft(), hostPort.getRight());
        if (socketStream == null) {
            logger.debug("Generating new Connection.");
            socketStream = connect(hostPort.getLeft(), hostPort.getRight());
        }

        //Can reuse the message, as it's the same data, just with an updated
        sendMessage(socketStream.socket, request);
    }

    @Override
    public void onHeartBeat(int type) {
        //send out a current summary of changes to the ChunkServer since the last heartbeat

        Event event = type == HEARTBEAT_MAJOR ? constructMajorHeartbeat() : constructMinorHeartbeat();

        // get controller socket, because that is always the initial connection used, it's
        // the first index in the known connection list
        sendMessage(connectionMetadata.getSocketStream(0).socket, event);
    }

    private Event constructMajorHeartbeat() {


        return null;

    }

    private Event constructMinorHeartbeat() {
        return null;
    }
}
