package filesystem.node;

import filesystem.heartbeat.HeartBeatScheduler;
import filesystem.interfaces.Command;
import filesystem.interfaces.Event;
import filesystem.interfaces.Record;
import filesystem.node.metadata.ChunkMetadata;
import filesystem.node.metadata.ClusterMetadataHandler;
import filesystem.node.metadata.FileMetadata;
import filesystem.protocol.events.*;
import filesystem.protocol.records.ChunkAdd;
import filesystem.transport.ContactList;
import filesystem.transport.SocketWrapper;
import filesystem.util.HostPortAddress;
import filesystem.util.LogConfiguration;
import filesystem.util.NodeUtils;
import filesystem.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static filesystem.protocol.Protocol.*;
import static filesystem.util.NodeUtils.appendLn;


public class Controller extends Node {
    private static final Logger logger = LogManager.getLogger(Controller.class);
    private static final int REPLICATION_FACTOR = Properties.getInt("REPLICATION_FACTOR");
    private final ClusterMetadataHandler clusterHandler;
    private HeartBeatScheduler timer;
    private CountDownLatch activeChunkHolders;

    public Controller(int port) {
        super(port);
        this.clusterHandler = new ClusterMetadataHandler();
    }

    public static void main(String[] args) {
        LogConfiguration.setSystemLogLevel(Properties.get("LOG_LEVEL"));

        // Read in the port from the config and start up the controller
        int listening_port = Properties.getInt("CONTROLLER_PORT");

        new Controller(listening_port);
    }

    @Override
    public void cleanup() {
        connectionHandler.cleanup();
//        stopChunkHolders();
//        timer.stopTasks();
    }

    @Override
    public void onLostConnection(Socket socket) {
        this.clusterHandler.removeBySocket(socket);
    }

    @Override
    public Map<String, Command> getCommandList() {
        Map<String, Command> commandMap = new HashMap<>();

        commandMap.put("files", userInput -> listClusterFiles());
        commandMap.put("init", userInput -> initialize());
        commandMap.put("stop", userInput -> stopChunkHolders());
        commandMap.put("show-config", userInput -> showConfig());
        commandMap.put("file-info", this::fileInfo);

        return commandMap;
    }

    @Override
    protected void resolveEventMap() {
        // ChunkHolder -> Controller
        this.eventCallbacks.put(CHUNK_SERVER_REQUESTS_REGISTRATION, this::chunkServerRegistration);
        this.eventCallbacks.put(CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS, this::deregistrationResponse);
        this.eventCallbacks.put(CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT, this::receiveMajorBeat);
        this.eventCallbacks.put(CHUNK_SERVER_SENDS_MINOR_HEARTBEAT, this::receiveMinorBeat);
        this.eventCallbacks.put(CHUNK_SERVER_REPORTS_HEALTH_HEARTBEAT, this::receiveHealthStatus);
        // Client -> Controller
        this.eventCallbacks.put(CLIENT_REQUESTS_FILE_ADD, this::fileAdd);
        this.eventCallbacks.put(CLIENT_REQUESTS_FILE_DELETE, this::fileDelete);
        this.eventCallbacks.put(CLIENT_REQUESTS_FILE, this::fileGet);
        this.eventCallbacks.put(CLIENT_REQUESTS_FILE_LIST, this::fileList);
        //this.eventActions.put(CLIENT_REQUESTS_CHUNK_SERVER_METADATA, this::chunkServerData);
    }

    private void chunkServerRegistration(Event e, Socket ignoredSocket) {
        ChunkHolderRequestsRegistration request = (ChunkHolderRequestsRegistration) e;

//        TODO: Throw a try except in here to catch the exception if the connection fails
//         and ignore the registration request / send back a failure response

        // Return values
        SocketWrapper holderConnection = null;
        boolean success = false;

        try {
            holderConnection = connectionHandler.connect(request.getHolderAddress());
            connectionHandler.addConnection(ignoredSocket);
            clusterHandler.addServer(request.getServerName(), request.getHolderAddress(), holderConnection.socket);
            Event event = new ControllerReportsRegistrationStatus(RESPONSE_SUCCESS);
            sendMessage(holderConnection, event);
        } catch (IOException | RuntimeException ex) {
            // Clean up any stored metadata about the connection
            connectionHandler.removeConnection(holderConnection);

        }


    }

    private void deregistrationResponse(Event e, Socket socket) {
        ChunkHolderReportsDeregistrationStatus response = (ChunkHolderReportsDeregistrationStatus) e;

        boolean removed;
        synchronized (clusterHandler.getServers()) {
            removed = clusterHandler.removeBySocket(socket);
        }

        if (removed) logger.info(String.format("%s successfully showdown.", response.getName()));
        else logger.error(String.format("%s unsuccessfully showdown.", response.getName()));

        // confirm the shutdown request
        Event event = new ControllerReportsShutdown();
        sendMessage(socket, event);

        activeChunkHolders.countDown();
    }

    private void receiveMajorBeat(Event e, Socket socket) {
        ChunkHolderSendsMajorHeartbeat heartbeat = (ChunkHolderSendsMajorHeartbeat) e;
        HostPortAddress holderAddress = NodeUtils.socketToHostPortAddress(socket);
        System.out.println(heartbeat);
        for (ChunkMetadata cmd : heartbeat.getCurrentChunks()) {
            ChunkLocationMetadata clmd = (ChunkLocationMetadata) clusterHandler.getFile(cmd.fileName).getChunkMetadata(cmd.chunkNumber);

            if (!clmd.serversHoldingChunk.contains(holderAddress)) {
                clmd.serversHoldingChunk.add(holderAddress);
            }

        }

        // TODO: Verify current metadata with info from the heartbeat. This information will then
        //  allow a scheduled task to run and check that the system is healthy (replication rules are followed, etc)


    }

    private void receiveMinorBeat(Event e, Socket socket) {
        ChunkHolderSendsMinorHeartbeat heartbeat = (ChunkHolderSendsMinorHeartbeat) e;
        HostPortAddress holderAddress = NodeUtils.socketToHostPortAddress(socket);
        System.out.println(heartbeat);

        for (Record r : heartbeat.getRecentRecords()) {
            ChunkAdd addRecord = (ChunkAdd) r;
            FileMetadata fmd = clusterHandler.getFile(addRecord.filePath);

            // Add it to the list of servers known to have the file
            ChunkLocationMetadata clmd = (ChunkLocationMetadata) fmd.getChunkMetadata(addRecord.chunkNumber);

            if (clmd == null) {
                clmd = new ChunkLocationMetadata(addRecord.filePath, addRecord.chunkNumber, addRecord.chunkSize, addRecord.hash, new ArrayList<>());
                fmd.addChunkMetadata(addRecord.chunkNumber, clmd);
            }

            clmd.serversHoldingChunk.add(holderAddress);

        }

        // TODO: Update current metadata with info from heartbeat.

    }

    private void receiveHealthStatus(Event unusedE, Socket socket) {
        //ChunkHolderReportsHeartbeat hb = (ChunkHolderReportsHeartbeat) e;
        clusterHandler.updateHeartBeatBySocket(socket);
    }

    /**
     * Respond with a current list of ChunkHolders to open a connection to send chunks
     *
     * @param e      The event that contains the file being requested to be added
     * @param socket Socket connection of the client
     */
    private void fileAdd(Event e, Socket socket) {
        ClientRequestsFileAdd request = (ClientRequestsFileAdd) e;
        logger.debug(String.format("Client Requests to add file: %s, Size: %d, Chunks: %d", request.getFile(), request.getFileSize(), request.getNumberOfChunks()));

        ArrayList<ClusterMetadataHandler.HolderMetadata> serverList = clusterHandler.getServers();
        ArrayList<ContactList> chunkDestinations = new ArrayList<>();
        ArrayList<HostPortAddress> selectedServers = new ArrayList<>();

        clusterHandler.addFile(request.getFile(), request.getNumberOfChunks(), request.getFileSize());

        //for each chunk, generate a random list of servers to contact
        for (int i = 1; i <= request.getNumberOfChunks(); i++) {

            //Generate a random list of distinct ints from 0 to n-1 ChunkHolders, then grab k amount needed for replication
            List<Integer> randomServerIndexes = new Random().ints(0, serverList.size()).distinct().limit(REPLICATION_FACTOR).boxed().collect(Collectors.toList());

            for (Integer serverIndex : randomServerIndexes) {
                selectedServers.add(serverList.get(serverIndex).address);
            }
            //add to the running list of chunk destinations and reset for the next chunk
            chunkDestinations.add(new ContactList(i, new ArrayList<>(selectedServers)));
            selectedServers.clear();
        }

        Event response = new ControllerReportsChunkAddList(RESPONSE_SUCCESS, request.getFile(), chunkDestinations);
        sendMessage(socket, response);
    }

    /**
     * Respond with a status as to whether the delete was successful, or unsuccessful (FileNotFound?)
     *
     * @param e            the event containing the file that the client wants deleted
     * @param unusedSocket Socket connection of the client
     */
    private void fileDelete(Event e, Socket unusedSocket) {
        //See if file exists in the system, and send out requests to delete it, if it exists
        ClientRequestsFileDelete clientRequest = (ClientRequestsFileDelete) e;

        String fileToDelete = clientRequest.getFile();


        FileMetadata fmd = clusterHandler.getFile(fileToDelete);
        if (fmd != null) {
            // For each chunk location, send out a request to delete it
            for (ChunkMetadata chunkMetadata : fmd.chunkList.values()) {
                ChunkLocationMetadata locationChunk = (ChunkLocationMetadata) chunkMetadata;
                for (HostPortAddress url : locationChunk.serversHoldingChunk) {
                    Socket chunkSocket = clusterHandler.getServer(url).socket;
                    ControllerRequestsChunkDelete request = new ControllerRequestsChunkDelete(fileToDelete, chunkMetadata.chunkNumber);
                    sendMessage(chunkSocket, request);
                }
            }
        }

        // TODO: Cache this command, and lock the metadata so that another command can't change state while it's being
        //  handled. At the same time, maybe block on the client, waiting for the OK from the Controller
    }

    /**
     * Respond with a current list of ChunkHolders containing all different chunks of the file
     *
     * @param e      The event containing the file the client wants
     * @param socket Socket connection of the client
     */
    private void fileGet(Event e, Socket socket) {
        ClientRequestsFile request = (ClientRequestsFile) e;

        // Look at current list of files in system, and respond with the list of servers associated with the requested
        // file, or respond with a negative status in the case of an absence of that file

        //TODO: Logic to check for file existence + get servers hosting chunks of said file
        int status = RESPONSE_FAILURE;
        ArrayList<ContactList> chunkList = null;

        FileMetadata fmd = clusterHandler.getFile(request.getFilePath());
        if (fmd != null) {
            status = RESPONSE_SUCCESS;
            chunkList = getChunkLocations(request.getFilePath());
        }

        Event response = new ControllerReportsChunkGetList(status, chunkList);
        sendMessage(socket, response);
    }

    /**
     * Packages and sends off the current file information the Controller is keeping to the client
     *
     * @param unusedE Event that contains a ClientRequestsFileList, contains an optional path parameter to search through
     * @param socket  Socket connection of the client
     */
    private void fileList(Event unusedE, Socket socket) {
        logger.debug("Controller received FileList request");
        //ClientRequestsFileList request = (ClientRequestsFileList) e;
        //TODO: logic to handle getting the files in the system, or under the optional path

        List<String> files = clusterHandler.getFiles().stream().map(FileMetadata::toString).collect(Collectors.toList());

        Event response = new ControllerReportsFileList(RESPONSE_SUCCESS, (ArrayList<String>) files);
        sendMessage(socket, response);
    }

    public ArrayList<ContactList> getChunkLocations(String filePath) {
        return (ArrayList<ContactList>) clusterHandler.getFile(filePath).chunkList.values().stream().map(chunk -> {
            ChunkLocationMetadata locationChunk = (ChunkLocationMetadata) chunk;
            return new ContactList(locationChunk.chunkNumber, locationChunk.serversHoldingChunk);
        }).collect(Collectors.toList());
    }

    private String listClusterFiles() {
        StringBuffer sb = new StringBuffer();

        for (FileMetadata fmd : clusterHandler.getFiles()) {
            sb.append(String.format("FileName: %s, FileSize: %d%n", fmd.fileName, fmd.fileSize));
            List<ChunkMetadata> chunks = new ArrayList<>(fmd.chunkList.values());
            sb.append(NodeUtils.GenericListFormatter.getFormattedOutput(chunks, "|", true));
        }

        return sb.toString();

        //return Utils.GenericListFormatter.getFormattedOutput(
        //        new ArrayList<>(clusterHandler.getFiles()), "|", true);
    }

    /**
     * Send out a command to each server and start a chunkServer there. Currently, this is
     * all written to work locally, but simulates a cluster through sockets and (in the future) different filepaths
     */
    private String initialize() {
        // TODO: Remove all init functionality. the tmux script is too cool not to use

//        if (isActive)
//            return "Already active.";
//        isActive = true;
//        return "System is activated.";

        /*
        // Open a pseudo Unix environment to run a bash script that will then open more terminals
        // running the servers, this hard-codes it to a windows environment at the moment
        try {
            ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\Git\\bin\\bash.exe",
                    "-c", String.format("bash ./start_chunks.sh %s %d", hostname(), port));
            Process p = pb.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
         */
        return "Init Disabled, System is dynamic.";
    }

    /**
     * Returns all information currently loaded in regarding the configuration of the cluster
     */
    public String showConfig() {
        StringBuilder sb = new StringBuilder();
        ArrayList<ClusterMetadataHandler.HolderMetadata> smd = clusterHandler.getServers();

        appendLn(sb, String.format("Nodes: %d", smd.size()));
        sb.append(NodeUtils.GenericListFormatter.getFormattedOutput(new ArrayList<>(smd), "|", true));
        return sb.toString();
    }

    private String fileInfo(String userInput) {
        String[] tokens = userInput.split(" ");

        FileMetadata fmd = null;

        try {
            fmd = this.clusterHandler.getFile(tokens[1]);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        if (fmd != null) return fmd.toString();

        return "Invalid file.";
    }

    private String stopChunkHolders() {
//        if (!isActive) {
//            return "Cluster isn't currently active";
//        }
//        isActive = false;

        //Request that each server shutdown
        String response;
        try {
            synchronized (clusterHandler.getServers()) {
                activeChunkHolders = new CountDownLatch(clusterHandler.getServers().size());
                logger.info(String.format("Sending shutdown request to %d nodes.", activeChunkHolders.getCount()));

                Event event = new ControllerRequestsDeregistration();
                for (ClusterMetadataHandler.HolderMetadata smd : clusterHandler.getServers())
                    sendMessage(smd.socket, event);
            }
            activeChunkHolders.await();
            response = "All servers have responded, exiting";
        } catch (InterruptedException e) {
            e.printStackTrace();
            response = "Error. Server shutdown Failed.";
        }

        return response;
    }


    /**
     * Metadata for each chunk, contains the chunkNumber of the associated file,
     * and a list of ID's that when used with the ServerHandler, can get relevant details
     * Default size of Chunk is 64kb
     */
    public static class ChunkLocationMetadata extends ChunkMetadata {
        public ArrayList<HostPortAddress> serversHoldingChunk;

        public ChunkLocationMetadata(String fileName, int chunkNumber, int chunkSize, String chunkHash, ArrayList<HostPortAddress> serversHoldingChunk) {
            super(fileName, chunkNumber, chunkSize, chunkHash);
            this.serversHoldingChunk = serversHoldingChunk;

        }

        //TODO: Refactor so that the chunk hash info is generated in the constructor, and the data itself is passed in to
        // be processed


        @Override
        public String toString() {
            return "LiteChunkMetadata{" + "chunkNumber=" + chunkNumber + ", chunkSize=" + chunkSize + ", chunkHash='" + chunkHash + '\'' + ", serversHoldingChunk=" + serversHoldingChunk + '}';
        }
    }
}
