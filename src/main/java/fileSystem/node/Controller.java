package fileSystem.node;

import fileSystem.util.*;
import fileSystem.protocol.Event;
import fileSystem.protocol.events.*;
import fileSystem.transport.TCPServer;
import fileSystem.util.metadata.FileMetadata;
import fileSystem.util.metadata.ServerMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static fileSystem.protocol.Protocol.*;

public class Controller extends Node implements Heartbeat {
    private static final Logger logger = LogManager.getLogger(Controller.class);

    //properties
    private static final int REPLICATION_FACTOR = 3;
    private static final String[] commandList = {"files", "init", "stop", "config"};
    private final int port;
    private final ClusterInformationHandler clusterHandler;

    //control flow
    private boolean isActive;
    private HeartbeatHandler timer;

    // Synchronization for startup and shutdown
    private CountDownLatch activeChunkServers;

    public Controller(int port) {
        isActive = false;
        this.port = port;
        clusterHandler = new ClusterInformationHandler();
    }

    public static void main(String[] args) throws UnknownHostException {

        // Inputs (Eventually)
        boolean DEBUG = true;
        int port = Integer.parseInt(args[0]);

        //setup debug mode logging if parameter was passed
        if (DEBUG)
            LogConfiguration.debug();


        //get the hostname and IP address
        InetAddress ip = InetAddress.getLocalHost();
        String host = ip.getHostName();

        logger.info(String.format("IP: %s, Host: %s%n", ip.getHostAddress(), host));

        Controller controller = new Controller(port);
        controller.configure();

        // create a server thread to listen to incoming connections, the semaphore isn't currently used here, only in
        // the ChunkServer, but will if the controller needs to send off a connection upon startup

        Thread tcpServer = new Thread(new TCPServer(controller, port, null));
        tcpServer.start();

        //create the console
        Thread console = new Thread(new ConsoleParser(controller));
        console.start();
    }

    @Override
    public void onHeartBeat(int type) {
        //send out requests to each of the current ChunkServers to make sure no failures have occurred
        Event e = new ControllerRequestsFunctionalHeartbeat();


        for (ServerMetadata smd : clusterHandler.getServers()) {
            sendMessage(smd.socket, e);
        }
    }

    private void configure() {
        // Note: as this class uses a reference to the controller, it must be constructed after the Controller
        // constructor to make sure it's fully setup
        timer = new HeartbeatHandler(5, 9, this);
    }

    @Override
    public boolean handleCommand(String input) {
        boolean isValid = true;
        switch (input) {
            case "files":
                listClusterFiles();
                break;
            case "init":
            case "initialize":
                initialize();
                break;
            case "stop":
                stopChunkServers();
                break;
            case "config":
                showConfig();
                break;
            default:
                isValid = false;
        }
        return isValid;
    }

    private void listClusterFiles() {
        System.out.println("********************");

        for (FileMetadata fmd : clusterHandler.getFiles())
            System.out.println(fmd);

    }

    private void stopChunkServers() {
        if (!isActive) {
            System.out.println("Cluster isn't currently active");
            return;
        }
        isActive = false;
        timer.stop();

        //Request that each server shutdown
        try {
            synchronized (clusterHandler.getServers()) {
                activeChunkServers = new CountDownLatch(clusterHandler.getServers().size());
                logger.info(String.format("Sending shutdown request to %d nodes.", activeChunkServers.getCount()));

                Event event = new ControllerRequestsDeregistration();
                for (ServerMetadata smd : clusterHandler.getServers()) {
                    sendMessage(smd.socket, event);
                }

            }
            activeChunkServers.await();
            logger.debug("All servers have responded, exiting");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(Event e, Socket socket) {
        try {
            switch (e.getType()) {
                // ChunkServer -> Controller
                case CHUNK_SERVER_REQUESTS_REGISTRATION:
                    chunkServerRegistration(e, socket);
                    break;
                case CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS:
                    deregistrationResponse(e, socket);
                    break;
                case CHUNK_SERVER_SENDS_MINOR_HEARTBEAT:
                    updateMajorbeat(e, socket);
                    break;
                case CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT:
                    updateMinorbeat(e, socket);
                    break;
                // Client -> Controller
                case CLIENT_REQUESTS_FILE_ADD:
                    fileAdd(e, socket);
                case CLIENT_REQUESTS_FILE_DELETE:
                    fileDelete(e, socket);
                    break;
                case CLIENT_REQUESTS_FILE:
                    fileGet(e, socket);
                    break;
                case CLIENT_REQUESTS_FILE_LIST:
                    fileList(e, socket);
                    break;
                case CLIENT_REQUESTS_CHUNK_SERVER_METADATA:
                    break;
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }


    private void updateMinorbeat(Event e, Socket socket) {
        ChunkServerSendsMinorHeartbeat heartbeat = (ChunkServerSendsMinorHeartbeat) e;

    }


    private void updateMajorbeat(Event e, Socket socket) {
        ChunkServerSendsMajorHeartbeat heartbeat = (ChunkServerSendsMajorHeartbeat) e;



    }

    /**
     * Packages and sends off the current file information the Controller is keeping to the client
     *
     * @param e      Event that contains a ClientRequestsFileList, contains an optional path parameter to search through
     * @param socket
     */
    private void fileList(Event e, Socket socket) {
        logger.debug("Controller received FileList request");
        ClientRequestsFileList request = (ClientRequestsFileList) e;

        //TODO: logic to handle getting the files in the system, or under the optional path
        ArrayList<String> files = new ArrayList<>();

        Event response = new ControllerReportsFileList(RESPONSE_SUCCESS, files);
        sendMessage(socket, response);
    }

    /**
     * Respond with a current list of ChunkServers containing all different chunks of the file
     *
     * @param e      The event containing the file the client wants
     * @param socket
     */
    private void fileGet(Event e, Socket socket) {
        // Look at current list of files in system, and respond with the list of servers associated with the requested
        // file, or respond with a negative status in the case of an absence of that file

        //TODO: Logic to check for file existence + get servers hosting chunks of said file

        Event response = new ControllerReportsChunkGetList(RESPONSE_FAILURE, 0);
        sendMessage(socket, response);
    }

    /**
     * Respond with a status as to whether the delete was successful, or unsuccessful (FileNotFound?)
     *
     * @param e      the event containing the file that the client wants deleted
     * @param socket
     */
    private void fileDelete(Event e, Socket socket) {
        //See if file exists in the system, and send out requests to delete it, if it exists

        //TODO: Logic to check for file existence + get servers hosting chunks of said file

        //Event response = new ControllerReportsFileDeleteStatus(RESPONSE_FAILURE);
        //sendMessage(socket, response);
    }

    /**
     * Respond with a current list of ChunkServers to open a connection to send chunks
     *
     * @param e      the event that contains the file being requested to be added
     * @param socket
     */
    private void fileAdd(Event e, Socket socket) {
        ClientRequestsFileAdd request = (ClientRequestsFileAdd) e;
        logger.debug(String.format("Client Requests to add file: %s, Size: %d, Chunks: %d", request.getFile(),
                request.getFileSize(), request.getNumberOfChunks()));

        ArrayList<ServerMetadata> serverList = clusterHandler.getServers();
        ArrayList<ContactList> chunkDestinations = new ArrayList<>();
        ArrayList<String> selectedServers = new ArrayList<>();

        clusterHandler.addFile(request.getFile(), request.getNumberOfChunks(), request.getFileSize());

        //for each chunk, generate a random list of servers to contact
        for (int i = 1; i <= request.getNumberOfChunks(); i++) {

            //Generate a random list of distinct ints from 0 to n ChunkServers, then grab k amount needed for replication
            List<Integer> randomServerIndexes = new Random().ints(0, serverList.size())
                    .distinct().limit(REPLICATION_FACTOR).boxed().collect(Collectors.toList());
            //convert to actual server contact details and save

            for (Integer serverIndex : randomServerIndexes)
                selectedServers.add(String.format("%s:%d", serverList.get(serverIndex).host,
                        serverList.get(serverIndex).port));
            //add to the running list of chunk destinations and reset for the next chunk
            chunkDestinations.add(new ContactList(i, new ArrayList<>(selectedServers)));
            selectedServers.clear();
        }

        Event response = new ControllerReportsChunkAddList(RESPONSE_SUCCESS,
                request.getFile(), chunkDestinations);
        sendMessage(socket, response);
    }

    private void deregistrationResponse(Event e, Socket socket) {
        ChunkServerReportsDeregistrationStatus response = (ChunkServerReportsDeregistrationStatus) e;

        //create the relevant object to find using the overloaded equals operator for ChunkData
        boolean removed;
        synchronized (clusterHandler.getServers()) {
            removed = clusterHandler.removeBySocket(socket);
        }

        if (removed)
            logger.info(String.format("%s successfully showdown.", response.getName()));
        else
            logger.error(String.format("%s unsuccessfully showdown.", response.getName()));

        // confirm the shutdown request
        Event event = new ControllerReportsShutdown();
        sendMessage(socket, event);

        activeChunkServers.countDown();
    }

    private void chunkServerRegistration(Event e, Socket socket) throws IOException {
        ChunkServerRequestsRegistration request = (ChunkServerRequestsRegistration) e;

        clusterHandler.addServer(request.getServerName(), request.getHost(),
                request.getPort(), socket);

        logger.debug("Received Registration Request: " + socket);
        Event event = new ControllerReportsRegistrationStatus(RESPONSE_SUCCESS);
        sendMessage(socket, event);
    }

    @Override
    public String help() {
        return "Controller: This is the driver that organizes all communication for" +
                "the cluster. Setup of the cluster is done here, and clients all communicate " +
                "with this driver in order to store, update, or remove files. Available commands " +
                "are shown with 'commands'.";
    }

    @Override
    public String intro() {
        return "Distributed System Controller: Enter 'help' for more information on configuration " +
                "or 'init' to start up the cluster";
    }

    @Override
    public String[] commands() {
        return commandList;
    }

    @Override
    public void cleanup() {
        if (isActive)
            stopChunkServers();
        server.cleanup();
    }

    /**
     * Returns all information currently loaded in regarding the configuration of the cluster
     */
    public void showConfig() {
        System.out.println("**** NODES ****");
        System.out.println("Nodes: " + clusterHandler.getServers().size());
        for (ServerMetadata server : clusterHandler.getServers())
            System.out.println(server);
        System.out.println(" ************* ");
    }

    /**
     * Send out a command to each server and start a chunkServer there. Currently, this is
     * all written to work locally, but simulates a cluster through sockets and (in the future) different filepaths
     */
    private void initialize() {
        if (isActive) {
            System.out.println("Already active.");
            return;
        }
        isActive = true;

        // Open a pseudo Unix environment to run a bash script that will then open more terminals
        // running the servers, this hard-codes it to a windows environment at the moment
        try {
            ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\Git\\bin\\bash.exe",
                    "-c", String.format("bash ./start_chunks.sh %s %d", hostname(), port));
            Process p = pb.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
