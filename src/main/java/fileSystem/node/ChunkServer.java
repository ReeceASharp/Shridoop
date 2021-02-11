package fileSystem.node;

import fileSystem.protocol.Event;
import fileSystem.protocol.events.*;
import fileSystem.transport.SocketStream;
import fileSystem.transport.TCPReceiver;
import fileSystem.transport.TCPServer;
import fileSystem.util.*;
import fileSystem.util.metadata.FileChunkData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static fileSystem.protocol.Protocol.*;

public class ChunkServer extends Node implements Heartbeat {
    //private static final int SLICE_SIZE = 8192;


    private static final Logger logger = LogManager.getLogger(ChunkServer.class);

    //needed for console commands, not the most DRY
    final String[] commandList = {"list-files", "config"};
    private final String nickname;
    private final String homePath;
    private final FileHandler fileHandler;


    public ChunkServer(String nickname, String homePath) {
        this.nickname = nickname;
        this.homePath = homePath;
        this.fileHandler = new FileHandler(homePath);
    }

    public static void main(String[] args) {
        final String controllerHost = args[0];
        final int controllerPort = Integer.parseInt(args[1]);
        final int listenPort = Integer.parseInt(args[2]);
        final String nickname = args[3];
        final String homePath = args[4];


        logger.debug(String.format("Listen: %d, Nickname: %s, Path: %s, ConnectPort: %d, ConnectHost: %s",
                listenPort, nickname, homePath, controllerPort, controllerHost));

        //get an object reference to be able to call functions and organize control flow
        ChunkServer server = new ChunkServer(nickname, homePath);

        //create a server thread to listen to incoming connections
        Semaphore setupLock = new Semaphore(1);
        setupLock.tryAcquire();
        Thread tcpServer = new Thread(new TCPServer(server, listenPort, setupLock));
        tcpServer.start();

        //create the console, this may not be needed for the chunkServer, but could be useful for debugging
        Thread console = new Thread(new ConsoleParser(server));
        console.start();

        try {
            setupLock.acquire();
            sendRegistration(server, controllerHost, controllerPort);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Chunk Server wants to register with the Controller after setting up, throw a message at it to check
     *
     * @param node           The ChunkServer sending the registration request
     * @param controllerHost The hostname/IP of the ChunkServer
     * @param controllerPort The port of the ChunkServer
     * @throws IOException thrown if the socket creation fails for some reason
     */
    private static void sendRegistration(ChunkServer node, String controllerHost, int controllerPort) throws IOException {
        //logger.debug(String.format("SENDING REGISTRATION TO %s:%d", host, port));

        //construct the message, and get the bytes
        Event e = new ChunkServerRequestsRegistration(node.getNickname(),
                node.getServerHost(),
                node.getServerPort());

        //open a socket/connection with the Controller, and set variables to be referenced later
        Socket controllerSocket = new Socket(controllerHost, controllerPort);

        SocketStream ss = new SocketStream(controllerSocket);
        node.connectionHandler.addConnection(ss);
        //create a listener on this new connection to listen for future requests/responses
        Thread receiver = new Thread(new TCPReceiver(node, ss, node.server));
        receiver.start();

        //Send the message to the Registry to attempt registration
        //logger.debug(node.connectionHandler);
        node.sendMessage(controllerSocket, e);
    }

    /**
     * Print out the details of the ChunkServer in a formatted way
     */
    private void showConfig() {
        System.out.printf("ServerName: '%s', Path: '%s'%n" +
                        "%s%n",
                nickname, homePath, server);
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

            // ChunkServer -> ChunkServer
            case CHUNK_SERVER_REQUESTS_REPLICATION:
                break;
            case CHUNK_SERVER_REPORTS_REPLICATION:
                break;

            // Client -> ChunkServer
            case CLIENT_REQUESTS_FILE_CHUNK:
                sendFileChunk(e, socket);
                break;
            // Node -> ClientServer
            case NODE_SENDS_FILE_CHUNK:
                fileAdd(e, socket);
                break;
        }
    }

    private void fileAdd(Event e, Socket socket) {
        NodeSendsFileChunk request = (NodeSendsFileChunk) e;

        if (!request.getHash().equals(FileChunker.getChunkHash(request.getChunkData()))) {
            logger.error("SHA HASH DOES NOT MATCH PREVIOUS STAGE.");
            //TODO: possibly exit method early and send a request back to the node for another file
            // in which case this will be started again

        }

        System.out.println(String.format("Received new chunk: %s, %d bytes", request.getFileName(),
                request.getChunkData().length));
        String fileName = request.getFileName() + request.getChunkNumber();
        //store the file in the local directory for the ChunkServer
        fileHandler.storeFileChunk(fileName, request.getChunkData(), request.getHash());

        //update the contact details

        ArrayList<String> serversToContact = request.getServersToContact();
        if (serversToContact.isEmpty())
            return;

        String hostPort = serversToContact.get(0);
        serversToContact.remove(0);

        // Check if there is already a connection
        SocketStream socketStream = connectionHandler.getSocketStream(hostPort);
        if (socketStream == null) {
            try {
                String[] tokens = hostPort.split(":");
                // Open a connection with the chunk server
                socketStream = new SocketStream(new Socket(tokens[0], Integer.parseInt(tokens[1])));
                connectionHandler.addConnection(socketStream);

                Thread receiver = new Thread(new TCPReceiver(this, socketStream, server));
                receiver.start();

                //logger.debug("Opening connection to: " + socketStream);
            } catch (IOException unknownHostException) {
                unknownHostException.printStackTrace();
            }
        }

        //Can reuse the message, as it's the same data, just with an updated
        sendMessage(socketStream.socket, request);

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
     * Simply responding with the current status of the ChunkServer. For the most part the status shouldn't be
     * needed until more features are implemented. In the future error-checking/corrupted file chunks could be a status,
     * but at the moment simply sending a response to the Controller means the ChunkServer is still alive
     *
     * @param socket
     */
    private void respondWithStatus(Socket socket) {
        Event event = new ChunkServerReportsFunctionalHeartbeat(RESPONSE_SUCCESS);
        sendMessage(socket, event);
    }

    private void deregistration(Event e, Socket socket) {
        ControllerRequestsDeregistration request = (ControllerRequestsDeregistration) e;

        //TODO: look at ControllerRequestsDeregistration for future feature info

        //logger.debug("Received Deregistration request");

        //respond
        Event event = new ChunkServerReportsDeregistrationStatus(RESPONSE_SUCCESS, getServerHost(),
                getServerPort(), nickname);

        sendMessage(socket, event);
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
                listChunks();
                break;
            case "config":
                showConfig();
                break;
            default:
                isValid = false;
        }
        return isValid;
    }

    private void listChunks() {
        System.out.println("Home path: " + homePath);
        System.out.println("***********************");
        for (FileChunkData smd : fileHandler.getFileChunks()) {
            System.out.println(smd);
        }
        System.out.println("***********************");
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

    public String getNickname() {
        return nickname;
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
    public void onHeartBeat(int type) {
        //send out a current summary of changes to the ChunkServer since the last heartbeat

        Event event = type == HEARTBEAT_MAJOR ? constructMajorHeartbeat() : constructMinorHeartbeat();

        // get controller socket, because that is always the initial connection used, it's
        // the first index in the known connection list
        sendMessage(connectionHandler.getSocketStream(0).socket, event);
    }

    private Event constructMajorHeartbeat() {



        return null;

    }

    private Event constructMinorHeartbeat() {
        return null;
    }
}
