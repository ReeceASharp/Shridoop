package filesystem.node;

import filesystem.heartbeat.HeartBeatScheduler;
import filesystem.interfaces.Command;
import filesystem.interfaces.Event;
import filesystem.interfaces.HeartBeat;
import filesystem.interfaces.MetadataCache;
import filesystem.node.metadata.ChunkMetadata;
import filesystem.protocol.events.*;
import filesystem.transport.SocketWrapper;
import filesystem.util.FileChunker;
import filesystem.util.FileHandler;
import filesystem.util.HostPortAddress;
import filesystem.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static filesystem.protocol.Protocol.*;

public class ChunkHolder extends Node implements HeartBeat, MetadataCache {
    private static final Logger logger = LogManager.getLogger(ChunkHolder.class);

    //needed for console commands, not the most DRY
    private final String serverName;
    private final String homePath;
    private final FileHandler fileHandler;

    // Why have this here?
    private HeartBeatScheduler timer;

    public ChunkHolder(int listenPort, String serverName, String homePath) {
        super(listenPort);

        this.serverName = serverName;
        this.homePath = homePath;
        this.fileHandler = new FileHandler(homePath);
    }

    public static void main(String[] args) throws IOException {
        // The IP is set via an env variable since it's dynamically assigned by the docker container
        String ip = System.getenv("IP_ADDRESS");

        // Parse commandline args
        final String controllerHost = Properties.getEnv("CONTROLLER_HOST");
        final int controllerPort = Properties.getEnvInt("CONTROLLER_PORT");
        final int listenPort = Properties.getInt("");
        final String name = Properties.getEnv("CHUNK_HOLDER_NAME");
        final String storagePath = Properties.getEnv("CHUNK_SERVER_STORAGE_PATH");

        ChunkHolder server = new ChunkHolder(listenPort, name, storagePath);

        Files.createDirectories(Paths.get(storagePath));
//        server.setup(controllerHost, controllerPort, listenPort);

        server.init(controllerHost, controllerPort, listenPort);

        logger.debug(String.format("Listen: %d, name: %s, StoragePath: %s", listenPort, name, storagePath));
    }

    private void init(String controllerHost, int controllerPort, int listenPort) throws IOException {


//        this.timer = new HeartBeatScheduler(this.getClass().getName());


        SocketWrapper ss = this.connectionHandler.addConnection(new Socket(controllerHost, controllerPort));

//        TODO: Temp testing
//        this.timer.scheduleAndStart(new MajorMinorBeatTask(this, Properties.getInt("MINOR_BEATS_BEFORE_MAJOR")),
//                "HolderHeartBeat", Properties.getInt("CONTROLLER_HEARTBEAT_DELAY"), Properties.getInt("CONTROLLER_HEARTBEAT_SECONDS"));


        Event e = new ChunkHolderRequestsRegistration(this.getServerName(), new HostPortAddress(
                this.getServerHost(), this.getServerPort()));

        this.sendMessage(ss, e);
    }


    public String getServerName() {
        return serverName;
    }

    @Override
    public void cleanup() {
        logger.debug("Exiting" + this.getClass().getCanonicalName());
//        timer.stopTasks();
        connectionHandler.cleanup();
    }

    @Override
    public void onLostConnection(Socket ignored) {
        // Chances are good this will never need to be implemented,
    }

    @Override
    public Map<String, Command> getCommandList() {
        Map<String, Command> commandMap = new HashMap<>();

        commandMap.put("list-files", userInput -> listChunks());
        commandMap.put("config", userInput -> showConfig());

        return commandMap;
    }

    @Override
    protected void resolveEventMap() {
        this.eventCallbacks.put(CONTROLLER_REPORTS_REGISTRATION_STATUS, this::registrationStatus);
        this.eventCallbacks.put(CONTROLLER_REQUESTS_DEREGISTRATION, this::deRegistration);
        this.eventCallbacks.put(CONTROLLER_REPORTS_SHUTDOWN, this::handleShutdown);
        this.eventCallbacks.put(CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT, this::respondWithStatus);
        this.eventCallbacks.put(CLIENT_REQUESTS_FILE_CHUNK, this::sendFileChunk);
        this.eventCallbacks.put(NODE_SENDS_FILE_CHUNK, this::fileAdd);
    }

    private void registrationStatus(Event e, Socket ignoredSocket) {
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
                break;
        }

    }

    private void deRegistration(Event ignoredE, Socket socket) {
        Event event = new ChunkHolderReportsDeregistrationStatus(RESPONSE_SUCCESS, getServerHost(),
                getServerPort(), serverName);

        sendMessage(socket, event);
    }

    private void handleShutdown(Event e, Socket socket) {
        // Wrapping in a EventAction so that it can be called when the server controller requests a shutdown
        cleanup();
    }

    /**
     * Simply responding with the current status of the ChunkHolder. For the most part the status shouldn't be
     * needed until more features are implemented. In the future error-checking/corrupted file chunks could be a status,
     * but at the moment simply sending a response to the Controller means the ChunkHolder is still alive
     */
    private void respondWithStatus(Event ignoredE, Socket socket) {
        Event event = new ChunkHolderReportsHeartbeat(RESPONSE_SUCCESS);
        sendMessage(socket, event);
    }

    /**
     * Respond to the fileRequest from the client
     */
    private void sendFileChunk(Event e, Socket socket) {
        ClientRequestsFileChunk request = (ClientRequestsFileChunk) e;

        byte[] fileData = fileHandler.getFileData(request.getFile());

        if (fileData == null)
            return;

        Event event = new ChunkHolderSendsFileChunk(fileData);
        sendMessage(socket, event);
    }

    /**
     * Handles the adding of a file, called either from a Client node, or from another ChunkHolder
     * requesting a replication
     *
     * @param e             Base event passed in, is actually NodeSendsFileChunk with respective request values
     * @param ignoredSocket The socket the event came in on, not being used
     */
    private void fileAdd(Event e, Socket ignoredSocket) {
        NodeSendsFileChunk request = (NodeSendsFileChunk) e;

        logger.debug(String.format("Received new chunk: %s, %d bytes", request.getFileName(),
                request.getChunkData().length));


        // This may not be needed, the data is assured to be correct due to the rules of TCP
        if (!request.getHash().equals(FileChunker.hashBytes(request.getChunkData()))) {
            logger.error("Data does match origin. Requesting a new chunk.");
            // TODO: possibly exit method early and send a request back to the node for another file
            //  in which case this will be started again
        }

        // TODO: Compare the hash passed through to the one generated locally off of the chunk

        //store the file in the local directory for the ChunkHolder
        ChunkMetadata cdmd = new ChunkDataMetadata(request.getFileName(),
                request.getChunkNumber(), request.getHash(), request.getChunkData());
        fileHandler.storeFileChunk(cdmd, request.getChunkData(), true);


        ArrayList<HostPortAddress> serversToContact = request.getServersToContact();
        if (serversToContact.isEmpty()) {
            logger.debug("Completed Replication of file chunk: " + request.getFileName() + request.getChunkNumber());
            return;
        }

        HostPortAddress address = serversToContact.remove(0);
        SocketWrapper socketWrapper = connectionHandler.getSocketStream(address);
        if (socketWrapper == null) {
            logger.debug("Generating new Connection.");
            socketWrapper = connectionHandler.connect(address);
        }

        //Can reuse the object, as it's the same data, just with an updated data-structure
        sendMessage(socketWrapper.socket, request);
    }

    private String listChunks() {

        return String.valueOf(fileHandler);
    }

    /**
     * Print out the details of the ChunkHolder in a formatted way
     */
    private String showConfig() {
        return String.format("ServerName: '%s', " +
                        "Path: '%s'%n" +
                        "ConnectionHandler: %s%n",
                serverName, homePath, connectionHandler);
    }

    @Override
    public void onHeartBeat(int type) {
        Event event = type == HEARTBEAT_MAJOR ? constructMajorHeartbeat() : constructMinorHeartbeat();

        // Controller is always first connection in known connections
        sendMessage(connectionHandler.getSocketStream(0).socket, event);
    }

    private Event constructMajorHeartbeat() {
        return new ChunkHolderSendsMajorHeartbeat((ArrayList<ChunkMetadata>) fileHandler.getChunkMetadata(),
                fileHandler.getTotalChunks());
    }

    private Event constructMinorHeartbeat() {
        return new ChunkHolderSendsMinorHeartbeat(fileHandler.getRecentRecords());
    }

    @Override
    public void cacheInfo(String path) {
        // TODO: Store current state in a configurable location, could just mean wrapping access methods with
        //  file-accesses
    }

    @Override
    public void updateFromCache(String path) {
        // TODO: Pull from saved state, this will need to check the saved config against what is given at runtime/compile
    }


    /**
     * Used by the Holder
     */
    protected static class ChunkDataMetadata extends ChunkMetadata {
        // TODO: Use with Reed-Solomon
        private final ArrayList<SliceMetadata> sliceList;

        public ChunkDataMetadata(String fileName,
                                 int chunkNumber,
                                 String chunkHash,
                                 byte[] chunkData) {
            super(fileName, chunkNumber, chunkData.length, chunkHash);
            this.sliceList = SliceMetadata.generateSliceMetadata(chunkData);
        }

        /**
         * Slices of a chunk. Assumption is that there are multiple slices in a chunk.
         */
        protected static class SliceMetadata implements Serializable {
            //Default: 1024 * 8, 2^13
            public static final int SLICE_SIZE_BYTES = Properties.getInt("SLICE_SIZE_BYTES");

            public final int sliceNumber;
            public final int sliceSize;
            public final String checksum;

            public SliceMetadata(int sliceNumber, int sliceSize, String checksum) {
                this.sliceNumber = sliceNumber;
                this.sliceSize = sliceSize;
                this.checksum = checksum;
            }

            /**
             * The function that chops up the chunk into slices
             *
             * @param chunkData: The chunk being broken up
             * @return An array of metadata with each element being metadata about the chunk, in sequential order
             */
            private static ArrayList<SliceMetadata> generateSliceMetadata(byte[] chunkData) {
                int slices = chunkData.length / SLICE_SIZE_BYTES;
                if (chunkData.length % SLICE_SIZE_BYTES != 0)
                    slices++;

                byte[] buf = new byte[SLICE_SIZE_BYTES];

                ByteArrayInputStream byteStream = new ByteArrayInputStream(chunkData);
                ArrayList<SliceMetadata> metadata = new ArrayList<>();

                int read;
                for (int i = 1; i <= slices; i++) {
                    read = byteStream.read(buf, 0, SLICE_SIZE_BYTES);
                    if (read < SLICE_SIZE_BYTES)
                        buf = Arrays.copyOf(buf, read);
                    String checksum = FileChunker.hashBytes(chunkData);
                    metadata.add(new SliceMetadata(i, read, checksum));
                }
//                NodeUtils.GenericListFormatter.getFormattedOutput(metadata, "|", true);
                return metadata;
            }
        }


    }


}
