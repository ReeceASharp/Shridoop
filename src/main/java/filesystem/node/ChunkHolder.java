package filesystem.node;

import filesystem.console.ConsoleParser;
import filesystem.heartbeat.HeartBeat;
import filesystem.heartbeat.HeartBeatScheduler;
import filesystem.node.metadata.ChunkMetadata;
import filesystem.node.metadata.MetadataCache;
import filesystem.pool.Command;
import filesystem.protocol.Event;
import filesystem.protocol.events.*;
import filesystem.transport.SocketStream;
import filesystem.transport.TCPServer;
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

    public ChunkHolder(String serverName, String homePath) {
        super();

        this.serverName = serverName;
        this.homePath = homePath;
        this.fileHandler = new FileHandler(homePath);
    }

    public static void main(String[] args) throws IOException {
        // while true sleep for 5 seconds

        Map<String, String> env = System.getenv();
        System.out.println("env: " + env.toString());

        try {
            int _i = 0;
            while (_i++ < 1000000000) {
                System.out.println("Hello World");
                Thread.sleep(10000);
        }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Parse commandline args
        final String controllerHost = args[0];
        final int controllerPort = Integer.parseInt(args[1]);
        final int listenPort = Integer.parseInt(args[2]);
        final String serverName = args[3];
        final String homePath = args[4];

        ChunkHolder server = new ChunkHolder(serverName, homePath);
        server.setup(controllerHost, controllerPort, listenPort);

        logger.debug(String.format("Listen: %d, serverName: %s, StoragePath: %s", listenPort, serverName, homePath));
    }

    private void setup(String controllerHost, int controllerPort, int listenPort) throws IOException {
        Files.createDirectories(Paths.get(this.homePath));

        this.timer = new HeartBeatScheduler(this.getClass().getName());
        this.server = new TCPServer(this, listenPort);
        this.console = new ConsoleParser(this);

        SocketStream ss = new SocketStream(new Socket(controllerHost, controllerPort));
        this.connectionHandler.addConnection(ss);

//        TODO: Temp testing
//        this.timer.scheduleAndStart(new MajorMinorBeatTask(this, Properties.getInt("MINOR_BEATS_BEFORE_MAJOR")),
//                "HolderHeartBeat", Properties.getInt("CONTROLLER_HEARTBEAT_DELAY"), Properties.getInt("CONTROLLER_HEARTBEAT_SECONDS"));

        new Thread(this.server).start();
        new Thread(this.console).start();


        Event e = new ChunkHolderRequestsRegistration(this.getServerName(), new HostPortAddress(
                this.getServerHost(), this.getServerPort()));

        this.sendMessage(ss, e);
    }


    public String getServerName() {
        return serverName;
    }

    @Override
    public String help() {
        return "This is strictly used for development and to see system details locally. " +
                "Available commands are shown with 'commands'.";
    }

    @Override
    public String intro() {
        return "Distributed System ChunkHolder (DEV ONLY), type " +
                "'help' for more details: ";
    }

    @Override
    public void cleanup() {
        logger.debug("Exiting" + this.getClass().getCanonicalName());

        timer.stopTasks();
        server.cleanup();

        // Note: because the console has a Scanner waiting for an input from System.in,
        // the only real way to exit is via the System, which is fine considering the
        // server was shut down gracefully
        System.exit(0);
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
        this.eventFunctions.put(CONTROLLER_REPORTS_REGISTRATION_STATUS, this::registrationStatus);
        this.eventFunctions.put(CONTROLLER_REQUESTS_DEREGISTRATION, this::deRegistration);
        this.eventFunctions.put(CONTROLLER_REPORTS_SHUTDOWN, this::handleShutdown);
        this.eventFunctions.put(CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT, this::respondWithStatus);
        this.eventFunctions.put(CLIENT_REQUESTS_FILE_CHUNK, this::sendFileChunk);
        this.eventFunctions.put(NODE_SENDS_FILE_CHUNK, this::fileAdd);
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
        SocketStream socketStream = connectionHandler.getSocketStream(address);
        if (socketStream == null) {
            logger.debug("Generating new Connection.");
            socketStream = connect(address);
        }

        //Can reuse the object, as it's the same data, just with an updated data-structure
        sendMessage(socketStream.socket, request);
    }

    private String listChunks() {
        StringBuilder sb = new StringBuilder();

        sb.append(fileHandler);

        return sb.toString();
    }

    /**
     * Print out the details of the ChunkHolder in a formatted way
     */
    private String showConfig() {
        return String.format("ServerName: '%s', " +
                        "Path: '%s'%n" +
                        "Server%s%n",
                serverName, homePath, server);
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
