package filesystem.node;

import filesystem.protocol.Event;
import filesystem.protocol.events.*;
import filesystem.transport.SocketStream;
import filesystem.transport.TCPServer;
import filesystem.util.Command;
import filesystem.util.ConsoleParser;
import filesystem.util.ContactList;
import filesystem.util.FileChunker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static filesystem.protocol.Protocol.*;

public class Client extends Node {
    private static final Logger logger = LogManager.getLogger(Client.class);

    private final String controllerHost;
    private final int controllerPort;
    private final Map<String, String> intermediateFilePaths;
    private SocketStream controllerSocket;

    public Client(String host, int port) {
        super();

        this.controllerHost = host;
        this.controllerPort = port;
        this.intermediateFilePaths = new HashMap<>();
    }


    public static void main(String[] args) throws MalformedURLException {
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(host, port);
        client.setup();
    }

    private void setup() throws MalformedURLException {
        this.server = new TCPServer(this, 0);
        this.console = new ConsoleParser(this);

        new Thread(this.server).start();
        new Thread(this.console).start();

        // TODO: Refactor this to attempt to connect when entering a subset of commands
        this.controllerSocket = connect(new URL(String.format("%s:%d", controllerHost, controllerPort)));
    }

    @Override
    protected void resolveEventMap() {
        // Controller -> Client
        this.eventActions.put(CONTROLLER_REPORTS_FILE_LIST, this::displayStoredFiles);
        this.eventActions.put(CONTROLLER_REPORTS_CHUNK_GET_LIST, this::fetchChunks);
        this.eventActions.put(CONTROLLER_REPORTS_CHUNK_ADD_LIST, this::sendChunks);
        this.eventActions.put(CONTROLLER_REPORTS_FILE_REMOVE_STATUS, this::deleteStatus);
        // ChunkServer -> Client
        this.eventActions.put(CHUNK_SERVER_SENDS_FILE_CHUNK, this::displayStoredFiles);
    }

    @Override
    public String help() {
        return "Client: This is the interface that is used to connect to a currently running Controller. Using one " +
                       "of the commands [add,get,delete] and a file parameter to modify the information on the cluster.";
    }

    @Override
    public String intro() {
        return "Distributed System Client: Used to connect to a Controller, can 'get', 'add', and 'delete' files from " +
                       "the cluster. More information available via 'help'.";
    }

    @Override
    public void cleanup() {
        server.cleanup();
    }

    @Override
    public void onLostConnection(Socket socket) {
        //TODO: This might be a bit in the future, but when the client is receiving file chunks, and this is called,
        // queue up another chunk transfer from another node. This might not even be necessary as the thread organizing the
        // chunk fetch will already be aware of any shenanigans
    }

    @Override
    public Map<String, Command> getCommandList() {
        Map<String, Command> commandList = new HashMap<>();

        commandList.put("add", this::addFile);
        commandList.put("delete", this::deleteFile);
        commandList.put("get", this::getFile);
        commandList.put("list-files", this::listFile);

        return commandList;
    }

    @Override
    protected void cacheInfo() {
        // No info to cache
    }

    @Override
    protected void updateFromCache() {
        // No info to fetch
    }

    private String addFile(String userInput) {
        String[] tokens = userInput.split(" ");

        String cloudPath = tokens[2];
        Path filePath = Paths.get(tokens[1]).toAbsolutePath().normalize();
        int numOfChunks = FileChunker.getChunkNumber(filePath);
        long fileSize = FileChunker.getFileSize(filePath);

        // Save path so async response can find relevant file, kinda jank
        this.intermediateFilePaths.put(cloudPath, filePath.toString());

        sendMessage(controllerSocket, new ClientRequestsFileAdd(cloudPath, numOfChunks, fileSize));

        return String.format("Add file: '%s', local: %s, size: %d, chunks: %d.", cloudPath, filePath, fileSize, numOfChunks);
    }

    private String deleteFile(String userInput) {
        String[] tokens = userInput.split(" ");
        String fileName = tokens[1];
        sendMessage(controllerSocket, new ClientRequestsFileDelete(fileName));
        return String.format("Delete file: '%s'.", fileName);
    }

    private String getFile(String userInput) {
        String[] tokens = userInput.split(" ");
        String fileName = tokens[1];
        sendMessage(controllerSocket, new ClientRequestsFile(fileName));
        return String.format("Get file: '%s'.", fileName);
    }

    private String listFile(String userInput) {
        String[] tokens = userInput.split(" ");
        sendMessage(controllerSocket, new ClientRequestsFileList(tokens[0]));
        return "Retrieving System file list.";
    }

    private void displayStoredFiles(Event e, Socket socket) {
        ControllerReportsFileList response = (ControllerReportsFileList) e;

        if (response.getStatus() == RESPONSE_SUCCESS) {
            System.out.println("**** Files **** ");
            for (String file : response.getFiles())
                System.out.println(file);
            System.out.println("  ************  ");
        } else if (response.getStatus() == RESPONSE_FAILURE)
            System.out.println("Failure to handle list query.");
    }

    private void fetchChunks(Event e, Socket socket) {
        ControllerReportsChunkGetList response = (ControllerReportsChunkGetList) e;

        //TODO: handle logic of querying the ChunkServers
        logger.debug(response.getChunkLocations());

        // For each chunk in the fetchlist, pick a random server and send a
    }

    private void sendChunks(Event e, Socket socket) {
        ControllerReportsChunkAddList response = (ControllerReportsChunkAddList) e;

        try (FileInputStream fis = new FileInputStream(fetchFilePath(response.getFile()));
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            byte[] buffer = new byte[CHUNK_SIZE];

            for (ContactList chunkToSend : response.getChunkDestinations()) {
                byte[] bytesToSend = FileChunker.buildChunk(buffer, bis);

                //calculate initial hash, so it can be verified upon reaching its destination
                String shaHash = FileChunker.getChunkHash(bytesToSend);

                // Get the server at the front, the list is generated in a random order at the Controller,
                // so there's no reason to use further randomness
                URL url = chunkToSend.getServersToContact().remove(0);

                SocketStream socketStream = connectionHandler.getSocketStream(url);
                if (socketStream == null)
                    socketStream = connect(url);

                sendMessage(socketStream, new NodeSendsFileChunk(
                        response.getFile(),
                        chunkToSend.getChunkNumber(),
                        bytesToSend,
                        shaHash,
                        chunkToSend.getServersToContact()));
            }
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }

    /**
     * Displays whether the delete request went through
     *
     * @param e The event to be converted to ControllerReportsFileDeleteStatus
     */
    private void deleteStatus(Event e, Socket socket) {
        //ControllerReportsFileDeleteStatus response = (ControllerReportsFileDeleteStatus) e;
//
//        switch (response.getStatus()) {
//            case RESPONSE_SUCCESS:
//                break;
//            case RESPONSE_FAILURE:
//                break;
//
//        }
    }

    private String fetchFilePath(String cloudPath) {
        return intermediateFilePaths.get(cloudPath);
    }

}
