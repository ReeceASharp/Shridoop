package fileSystem.node;

import fileSystem.protocol.*;
import fileSystem.protocol.events.*;
import fileSystem.transport.*;
import fileSystem.util.Properties;
import fileSystem.util.*;
import fileSystem.util.metadata.*;
import org.apache.logging.log4j.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import static fileSystem.protocol.Protocol.*;
import static fileSystem.util.Utils.*;


public class Controller extends Node implements Heartbeat {


    private static final Logger logger = LogManager.getLogger(Controller.class);

    //Properties
    private static final int REPLICATION_FACTOR = Integer.parseInt(Properties.get("REPLICATION_FACTOR"));
    private final int port;

    // Metadata storage
    private final ClusterMetadataHandler clusterHandler;

    //Control flow
    private boolean isActive;
    private HeartbeatHandler timer;

    //Start - Stop synchronization
    private CountDownLatch activeChunkServers;

    public Controller(int port) {
        super();

        this.isActive = false;
        this.port = port;
        this.clusterHandler = new ClusterMetadataHandler();
    }

    public static void main(String[] args) throws UnknownHostException {
        if (Properties.get("DEBUG").equals("true"))
            LogConfiguration.debug();
        logger.info("Debug: " + Properties.get("DEBUG"));


        int port = Integer.parseInt(args[0]);
        Controller controller = new Controller(port);
        controller.setup();

        logger.info(String.format("IP: %s", controller.hostname()));

    }

    private void setup() {
        // Builds components outside of the constructor that require a reference to the parent to function
        this.timer = new HeartbeatHandler(5, 9, this);
        this.server = new TCPServer(this, port, null);
        this.console = new ConsoleParser(this);

        new Thread(this.server).start();
        new Thread(this.console).start();
    }

    @Override
    public void onHeartBeat(int type) {
        //send out requests to each of the current ChunkServers to make sure no failures have occurred
        Event e = new ControllerRequestsFunctionalHeartbeat();


        for (ServerMetadata smd : clusterHandler.getServers()) {
            sendMessage(smd.socket, e);
        }
    }

    @Override
    protected void associateEvents() {
        // ChunkServer -> Controller
        this.eventActions.put(CHUNK_SERVER_REQUESTS_REGISTRATION, this::chunkServerRegistration);
        this.eventActions.put(CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS, this::deregistrationResponse);
        this.eventActions.put(CHUNK_SERVER_SENDS_MINOR_HEARTBEAT, this::updateMajorbeat);
        this.eventActions.put(CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT, this::updateMinorbeat);
        // Client -> Controller
        this.eventActions.put(CLIENT_REQUESTS_FILE_ADD, this::fileAdd);
        this.eventActions.put(CLIENT_REQUESTS_FILE_DELETE, this::fileDelete);
        this.eventActions.put(CLIENT_REQUESTS_FILE, this::fileGet);
        this.eventActions.put(CLIENT_REQUESTS_FILE_LIST, this::fileList);
        //this.eventActions.put(CLIENT_REQUESTS_CHUNK_SERVER_METADATA, this::chunkServerData);
    }

    @Override
    public String help() {
        return "Controller: This is the driver that organizes all communication for" +
                       "the cluster. Setup of the cluster is done here, and clients all communicate " +
                       "with this driver in order to store, update, or remove files. Available commands " +
                       "are shown with 'commands'.";
    }

    //private ArrayList<>

    @Override
    public String intro() {
        return "Distributed System Controller: Enter 'help' for more information on configuration " +
                       "or 'init' to start up the cluster";
    }

    @Override
    public void cleanup() {
        if (isActive)
            stopChunkServers();
        server.cleanup();
    }

    private String stopChunkServers() {
        if (!isActive) {
            return "Cluster isn't currently active";
        }
        isActive = false;
        timer.stop();

        //Request that each server shutdown
        String response;
        try {
            synchronized (clusterHandler.getServers()) {
                activeChunkServers = new CountDownLatch(clusterHandler.getServers().size());
                logger.info(String.format("Sending shutdown request to %d nodes.", activeChunkServers.getCount()));

                Event event = new ControllerRequestsDeregistration();
                for (ServerMetadata smd : clusterHandler.getServers())
                    sendMessage(smd.socket, event);
            }
            activeChunkServers.await();
            response = "All servers have responded, exiting";
        } catch (InterruptedException e) {
            e.printStackTrace();
            response = "Error. Server shutdown Failed.";
        }

        return response;
    }

    @Override
    public Map<String, Command> getCommandList() {
        Map<String, Command> commandMap = new HashMap<>();

        commandMap.put("files", userInput -> listClusterFiles());
        commandMap.put("init", userInput -> initialize());
        commandMap.put("stop", userInput -> stopChunkServers());
        commandMap.put("show-config", userInput -> showConfig());
        commandMap.put("file-info", this::fileInfo);

        return commandMap;
    }

    private String listClusterFiles() {
        StringBuilder sb = new StringBuilder();
        appendLn(sb, "**** FILES ****");

        for (FileMetadata fmd : clusterHandler.getFiles())
            appendLn(sb, fmd.toString());

        appendLn(sb, " ************* ");

        return sb.toString();
    }

    /**
     * Send out a command to each server and start a chunkServer there. Currently, this is
     * all written to work locally, but simulates a cluster through sockets and (in the future) different filepaths
     */
    private String initialize() {
        // TODO: Remove all init functionality. the tmux script is too cool not to use

        if (isActive)
            return "Already active.";
        isActive = true;
        return "System is activated.";


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
    }

    /**
     * Returns all information currently loaded in regarding the configuration of the cluster
     */
    public String showConfig() {
        StringBuilder sb = new StringBuilder();
        appendLn(sb, "**** NODES ****");
        appendLn(sb, String.format("Nodes: %d", clusterHandler.getServers().size()));
        for (ServerMetadata server : clusterHandler.getServers())
            appendLn(sb, server.toString());
        appendLn(sb, " ************* ");

        return sb.toString();
    }

    private String fileInfo(String userInput) {
        String[] tokens = userInput.split(" ");

        FileMetadata fmd = this.clusterHandler.getFile(tokens[1]);
        if (fmd != null)
            return fmd.toString();

        return "File not in system.";
    }

    @Override
    protected void cacheInfo() {


    }

    @Override
    protected void updateFromCache() {

    }

    private void chunkServerRegistration(Event e, Socket socket) {
        ChunkServerRequestsRegistration request = (ChunkServerRequestsRegistration) e;

        clusterHandler.addServer(request.getServerName(), request.getHost(),
                request.getPort(), socket);

        Event event = new ControllerReportsRegistrationStatus(RESPONSE_SUCCESS);
        sendMessage(socket, event);
    }

    private void deregistrationResponse(Event e, Socket socket) {
        ChunkServerReportsDeregistrationStatus response = (ChunkServerReportsDeregistrationStatus) e;

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

    private void updateMajorbeat(Event e, Socket socket) {
        ChunkServerSendsMajorHeartbeat heartbeat = (ChunkServerSendsMajorHeartbeat) e;


    }

    private void updateMinorbeat(Event e, Socket socket) {
        ChunkServerSendsMinorHeartbeat heartbeat = (ChunkServerSendsMinorHeartbeat) e;

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
        ArrayList<Pair<String, Integer>> selectedServers = new ArrayList<>();

        clusterHandler.addFile(request.getFile(), request.getNumberOfChunks(), request.getFileSize());

        //for each chunk, generate a random list of servers to contact
        for (int i = 1; i <= request.getNumberOfChunks(); i++) {

            //Generate a random list of distinct ints from 0 to n ChunkServers, then grab k amount needed for replication
            List<Integer> randomServerIndexes = new Random().ints(0, serverList.size())
                                                        .distinct()
                                                        .limit(REPLICATION_FACTOR)
                                                        .boxed()
                                                        .collect(Collectors.toList());

            for (Integer serverIndex : randomServerIndexes) {
                String host = serverList.get(serverIndex).host;
                int port = serverList.get(serverIndex).port;
                selectedServers.add(new Pair(host, port));
            }
            //add to the running list of chunk destinations and reset for the next chunk
            chunkDestinations.add(new ContactList(i, new ArrayList<>(selectedServers)));
            selectedServers.clear();
        }

        Event response = new ControllerReportsChunkAddList(RESPONSE_SUCCESS,
                request.getFile(), chunkDestinations);
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
     * Respond with a current list of ChunkServers containing all different chunks of the file
     *
     * @param e      The event containing the file the client wants
     * @param socket
     */
    private void fileGet(Event e, Socket socket) {
        ClientRequestsFile request = (ClientRequestsFile) e;

        // Look at current list of files in system, and respond with the list of servers associated with the requested
        // file, or respond with a negative status in the case of an absence of that file

        //TODO: Logic to check for file existence + get servers hosting chunks of said file
        int status = RESPONSE_FAILURE;
        ArrayList<ContactList> chunkList = null;

        FileMetadata fmd = clusterHandler.getFile(request.getFile());
        if (fmd != null) {
            status = RESPONSE_SUCCESS;
            chunkList = fmd.getChunkLocations();
        }


        Event response = new ControllerReportsChunkGetList(status, chunkList);
        sendMessage(socket, response);
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

        List<String> files = clusterHandler.getFiles().stream().map(FileMetadata::toString).collect(Collectors.toList());

        Event response = new ControllerReportsFileList(RESPONSE_SUCCESS, (ArrayList<String>) files);
        sendMessage(socket, response);
    }
}
