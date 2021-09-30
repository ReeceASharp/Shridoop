package filesystem.node;

import filesystem.protocol.Event;
import filesystem.protocol.events.*;
import filesystem.transport.TCPServer;
import filesystem.util.Properties;
import filesystem.util.*;
import filesystem.util.metadata.FileMetadata;
import filesystem.util.metadata.LiteChunkMetadata;
import filesystem.util.metadata.ServerMetadata;
import filesystem.util.taskscheduler.HeartBeatTask;
import filesystem.util.taskscheduler.TaskScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static filesystem.protocol.Protocol.*;
import static filesystem.util.Utils.appendLn;


public class Controller extends Node implements HeartBeat {
    private static final Logger logger = LogManager.getLogger(Controller.class);
    private static final int REPLICATION_FACTOR = Integer.parseInt(Properties.get("REPLICATION_FACTOR"));
    private final int port;
    private final ClusterMetadataHandler clusterHandler;
    private boolean isActive;
    private TaskScheduler timer;
    private CountDownLatch activeChunkServers;

    public Controller(int port) {
        super();

        this.isActive = false;
        this.port = port;
        this.clusterHandler = new ClusterMetadataHandler();
    }

    public static void main(String[] args) {
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
        this.timer = new TaskScheduler(this.getClass().getName());
        this.server = new TCPServer(this, port);
        this.console = new ConsoleParser(this);

        new Thread(this.server).start();
        new Thread(this.console).start();

        this.timer.scheduleAndStart(new HeartBeatTask(this), "ChunkServerHealthStatus", 5, 30);
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
    protected void resolveEventMap() {
        // ChunkServer -> Controller
        this.eventActions.put(CHUNK_SERVER_REQUESTS_REGISTRATION, this::chunkServerRegistration);
        this.eventActions.put(CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS, this::deregistrationResponse);
        this.eventActions.put(CHUNK_SERVER_SENDS_MINOR_HEARTBEAT, this::receiveMajorbeat);
        this.eventActions.put(CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT, this::receiveMinorbeat);
        this.eventActions.put(CHUNK_SERVER_REPORTS_HEALTH_HEARTBEAT, this::receiveHealthStatus);
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

    @Override
    public String intro() {
        return "Distributed System Controller: Enter 'help' for more information on configuration " +
                       "or 'init' to start up the cluster";
    }

    @Override
    public void cleanup() {
        if (isActive)
            stopChunkServers();

        timer.stopTasks();
        server.cleanup();
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
        commandMap.put("stop", userInput -> stopChunkServers());
        commandMap.put("show-config", userInput -> showConfig());
        commandMap.put("file-info", this::fileInfo);

        return commandMap;
    }

    private String listClusterFiles() {
        return Utils.GenericListFormatter.getFormattedOutput(
                new ArrayList<>(clusterHandler.getFiles()), "|", true);
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
        ArrayList<ServerMetadata> smd = clusterHandler.getServers();

        appendLn(sb, String.format("Nodes: %d", smd.size()));
        sb.append(Utils.GenericListFormatter.getFormattedOutput(
                new ArrayList<>(smd), "|", true));
        return sb.toString();
    }

    private String fileInfo(String userInput) {
        String[] tokens = userInput.split(" ");

        FileMetadata fmd = null;

        try {
            fmd = this.clusterHandler.getFile(tokens[1]);
        } catch (ArrayIndexOutOfBoundsException ignored){ }

        if (fmd != null)
            return fmd.toString();

        return "Invalid file.";
    }

    @Override
    protected void cacheInfo() {


    }

    @Override
    protected void updateFromCache() {

    }

    private String stopChunkServers() {
        if (!isActive) {
            return "Cluster isn't currently active";
        }
        isActive = false;

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

    private void chunkServerRegistration(Event e, Socket socket) {
        ChunkServerRequestsRegistration request = (ChunkServerRequestsRegistration) e;

        clusterHandler.addServer(request.getServerName(), request.getURL(), socket);

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

    private void receiveMajorbeat(Event e, Socket socket) {
        ChunkServerSendsMajorHeartbeat heartbeat = (ChunkServerSendsMajorHeartbeat) e;

        // TODO: Verify current metadata with info from the heartbeat. This information will then
        //  allow a scheduled task to run and check that the system is healthy (replication rules are followed, etc)


    }

    private void receiveMinorbeat(Event e, Socket socket) {
        ChunkServerSendsMinorHeartbeat heartbeat = (ChunkServerSendsMinorHeartbeat) e;

        // TODO: Update current metadata with info from heartbeat.

    }

    private void receiveHealthStatus(Event e, Socket socket) {
        //ChunkServerReportsHeartbeat hb = (ChunkServerReportsHeartbeat) e;
        clusterHandler.updateHeartBeatBySocket(socket);
    }

    /**
     * Respond with a current list of ChunkServers to open a connection to send chunks
     *
     * @param e      The event that contains the file being requested to be added
     * @param socket Socket connection of the client
     */
    private void fileAdd(Event e, Socket socket) {
        ClientRequestsFileAdd request = (ClientRequestsFileAdd) e;
        logger.debug(String.format("Client Requests to add file: %s, Size: %d, Chunks: %d", request.getFile(),
                request.getFileSize(), request.getNumberOfChunks()));

        ArrayList<ServerMetadata> serverList = clusterHandler.getServers();
        ArrayList<ContactList> chunkDestinations = new ArrayList<>();
        ArrayList<URL> selectedServers = new ArrayList<>();

        clusterHandler.addFile(request.getFile(), request.getNumberOfChunks(), request.getFileSize());

        //for each chunk, generate a random list of servers to contact
        for (int i = 1; i <= request.getNumberOfChunks(); i++) {

            //Generate a random list of distinct ints from 0 to n-1 ChunkServers, then grab k amount needed for replication
            List<Integer> randomServerIndexes = new Random().ints(0, serverList.size())
                                                        .distinct()
                                                        .limit(REPLICATION_FACTOR)
                                                        .boxed()
                                                        .collect(Collectors.toList());

            for (Integer serverIndex : randomServerIndexes) {
                selectedServers.add(serverList.get(serverIndex).url);
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
     * @param socket Socket connection of the client
     */
    private void fileDelete(Event e, Socket socket) {
        //See if file exists in the system, and send out requests to delete it, if it exists
        ClientRequestsFileDelete clientRequest = (ClientRequestsFileDelete) e;

        String fileToDelete = clientRequest.getFile();


        FileMetadata fmd = clusterHandler.getFile(fileToDelete);
        if (fmd != null) {
            // For each chunk location, send out a request to delete it
            for (LiteChunkMetadata chunkMetadata : fmd.chunkList) {
                for (URL url : chunkMetadata.serversHoldingChunk) {
                    Socket chunkSocket = clusterHandler.getServer(url).socket;
                    ControllerRequestsChunkDelete request = new ControllerRequestsChunkDelete(
                            fileToDelete, chunkMetadata.chunkNumber);
                    sendMessage(chunkSocket, request);
                }
            }
        }

        // TODO: Cache this command, and lock the metadata so that another command can't change state while it's being
        //  handled. At the same time, maybe block on the client, waiting for the OK from the Controller
    }

    /**
     * Respond with a current list of ChunkServers containing all different chunks of the file
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
     * @param socket Socket connection of the client
     */
    private void fileList(Event e, Socket socket) {
        logger.debug("Controller received FileList request");
        //ClientRequestsFileList request = (ClientRequestsFileList) e;
        //TODO: logic to handle getting the files in the system, or under the optional path

        List<String> files = clusterHandler.getFiles().stream().map(FileMetadata::toString).collect(Collectors.toList());

        Event response = new ControllerReportsFileList(RESPONSE_SUCCESS, (ArrayList<String>) files);
        sendMessage(socket, response);
    }
}
