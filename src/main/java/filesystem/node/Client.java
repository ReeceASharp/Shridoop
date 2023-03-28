package filesystem.node;

import filesystem.console.ConsoleParser;
import filesystem.interfaces.Command;
import filesystem.interfaces.CommandInterface;
import filesystem.interfaces.Event;
import filesystem.protocol.events.*;
import filesystem.transport.ContactList;
import filesystem.transport.SocketWrapper;
import filesystem.util.FileChunker;
import filesystem.util.HostPortAddress;
import filesystem.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


import static filesystem.protocol.Protocol.*;

public class Client extends Node implements CommandInterface {
    private static final Logger logger = LogManager.getLogger(Client.class);

    private final HostPortAddress controllerAddress;
    private final Map<String, String> intermediateFilePaths;
    private SocketWrapper controllerSocket;
    private ConsoleParser console;

    public Client(String controllerHost, int controllerPort) {
        // Listening port of 0 means that the OS will assign a port, doesn't matter for clients
        // since they're always the ones initially setting up connections
        super(0);

        this.controllerAddress = new HostPortAddress(controllerHost, controllerPort);
        this.intermediateFilePaths = new HashMap<>();
    }


    public static void main(String[] args) {
        String controllerHost = Properties.get("CONTROLLER_HOST");
        int controllerPort = Properties.getInt("CONTROLLER_PORT");

        Client client = new Client(controllerHost, controllerPort);

        client.init();

    }

    private void init() {
        controllerSocket = connectionHandler.connect(controllerAddress);
        this.console = new ConsoleParser(this);
        new Thread(this.console).start();

    }

    @Override
    protected void resolveEventMap() {
        // Controller -> Client
        this.eventCallbacks.put(CONTROLLER_REPORTS_FILE_LIST, this::displayStoredFiles);
        this.eventCallbacks.put(CONTROLLER_REPORTS_CHUNK_GET_LIST, this::fetchChunks);
        this.eventCallbacks.put(CONTROLLER_REPORTS_CHUNK_ADD_LIST, this::sendChunks);
        this.eventCallbacks.put(CONTROLLER_REPORTS_FILE_REMOVE_STATUS, this::deleteStatus);
        // ChunkHolder -> Client
        this.eventCallbacks.put(CHUNK_SERVER_SENDS_FILE_CHUNK, this::displayStoredFiles);
    }

    @Override
    public void cleanup() {
        connectionHandler.cleanup();
    }

    @Override
    public void onLostConnection(Socket socket) {
        //TODO: This might be a bit in the future, but when the client is receiving file chunks, and this is called,
        // queue up another chunk transfer from another node. This might not even be necessary as the thread organizing the
        // chunk fetch will already be aware of any shenanigans
    }

    public Map<String, Command> getCommandList() {
        Map<String, Command> commandList = new HashMap<>();

        commandList.put("add", this::addFile);
        commandList.put("delete", this::deleteFile);
        commandList.put("get", this::getFile);
        commandList.put("list-files", this::listFile);

        return commandList;
    }

    @Override
    public String help() {
        return null;
    }

    @Override
    public String intro() {
        return null;
    }

    @Override
    public void exit() {
        cleanup();
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

        //TODO: Improve logic of querying the ChunkHolders, go by logical file-system size
        // (each gets simulated 500Mb, etc)

        // For each chunk in the fetch list, pick a random server and send a request
        for (ContactList locations : response.getChunkLocations()) {
            // Pick one and send a request for a fetch from it
//            TODO: Randomize server
            HostPortAddress location = locations.getServersToContact().get(0);
        }

//        TODO: Possibly implement console block func to wait until file is received and reconstructed
    }

    private void sendChunks(Event e, Socket socket) {
        ControllerReportsChunkAddList response = (ControllerReportsChunkAddList) e;

        try (FileInputStream fis = new FileInputStream(fetchFilePath(response.getFile()));
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            byte[] buffer = new byte[CHUNK_SIZE];

            for (ContactList chunkToSend : response.getChunkDestinations()) {
                byte[] bytesToSend = FileChunker.chunkFile(buffer, bis);

                //calculate initial hash, so it can be verified upon reaching its destination
                String shaHash = FileChunker.hashBytes(bytesToSend);

                // Get the server at the front, the list is generated in a random order at the Controller,
                // so there's no reason to use further randomness
                HostPortAddress address = chunkToSend.getServersToContact().remove(0);

                SocketWrapper socketWrapper = connectionHandler.getSocketStream(address);
                if (socketWrapper == null)
                    socketWrapper = connectionHandler.connect(address);

                sendMessage(socketWrapper, new NodeSendsFileChunk(
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
