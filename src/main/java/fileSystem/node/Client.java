package fileSystem.node;

import fileSystem.protocol.*;
import fileSystem.protocol.events.*;
import fileSystem.transport.*;
import fileSystem.util.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import static fileSystem.protocol.Protocol.*;

public class Client extends Node {
    private static final Logger logger = LogManager.getLogger(Client.class);

    private final String controllerHost;
    private final int controllerPort;
    private Socket controllerSocket;
    private final Map<String, String> intermediateFilePaths;

    public Client(String host, int port) {
        this.controllerHost = host;
        this.controllerPort = port;

        this.intermediateFilePaths = new HashMap<>();
    }


    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(host, port);

        //create a server thread to listen to incoming connections
        Thread tcpServer = new Thread(new TCPServer(client, 0, null));
        tcpServer.start();

        //Console parser
        Thread console = new Thread(new ConsoleParser(client));
        console.start();
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
    public void onEvent(Event e, Socket socket) {
        switch (e.getType()) {
            // Controller -> Client
            case CONTROLLER_REPORTS_FILE_LIST:
                displayStoredFiles(e);
                break;
            case CONTROLLER_REPORTS_CHUNK_GET_LIST:
                receiveChunkFetchList(e);
                break;
            case CONTROLLER_REPORTS_CHUNK_ADD_LIST:
                receiveChunkAddList(e);
                break;
            case CONTROLLER_REPORTS_FILE_REMOVE_STATUS:
                deleteStatus(e);
                break;

            // ChunkServer -> Client
            case CHUNK_SERVER_SENDS_FILE_CHUNK:
                break;
        }

    }

    private void displayStoredFiles(Event e) {
        ControllerReportsFileList response = (ControllerReportsFileList) e;

        if (response.getStatus() == RESPONSE_SUCCESS) {
            System.out.println("**** Files **** ");
            for (String file : response.getFiles()) {
                System.out.println(file);
            }
            System.out.println("  ************  ");
        } else if (response.getStatus() == RESPONSE_FAILURE) {
            System.out.println("Failure to handle list query.");
        }
    }

    private void receiveChunkFetchList(Event e) {
        ControllerReportsChunkGetList response = (ControllerReportsChunkGetList) e;

        //TODO: handle logic of querying the ChunkServers
    }

    private void receiveChunkAddList(Event e) {
        ControllerReportsChunkAddList response = (ControllerReportsChunkAddList) e;
        logger.debug("Received list, Sending out file partitions to chunkServers.");

        // Setup file input streams to handle the chunk creation of the file,


        try (FileInputStream fis = new FileInputStream(fetchFilePath(response.getFile()));
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            int bytesRead;
            //maximum size of chunk
            byte[] buffer = new byte[CHUNK_SIZE];

            // Dynamically fetch data from the file in chunksize increments, we already calculated and passed the
            // # of chunks to the Controller, and it responded with a set of servers for each chunk to be sent to
            for (ContactList chunkToSend : response.getChunkDestinations()) {
                // Get chunk data, and copy into a dynamically sized array to minimize chunk size
                // this is only really useful for the last chunk of a large file, or the only
                // chunk of a small file
                bytesRead = bis.read(buffer);
                byte[] bytesToSend = new byte[bytesRead];
                System.arraycopy(buffer, 0, bytesToSend, 0, bytesRead);
                logger.debug(String.format("Bytes Read: %d", bytesRead));

                //calculate initial hash, so it can be verified upon reaching its destination
                String shaHash = FileChunker.getChunkHash(bytesToSend);


                // Get the server at the front, the list is generated in a random order at the Controller,
                // so there's no reason to use further randomness
                ArrayList<String> serverList = chunkToSend.getServersToContact();
                String hostPort = serverList.get(0);
                // Remove it from the list as it's the one being contacted
                serverList.remove(0);

                // TODO: refactor stream generation to be inside of the messageSend
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

                NodeSendsFileChunk chunkSend = new NodeSendsFileChunk(response.getFile(),
                        chunkToSend.getChunkNumber(), bytesToSend, shaHash, serverList);
                sendMessage(socketStream.socket, chunkSend);
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
    private void deleteStatus(Event e) {
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

    @Override
    public void cleanup() {
        server.cleanup();
    }

    @Override
    public Map<String, Command> getCommandMap() {
        Map<String, Command> commandMap = new HashMap<>();

        commandMap.put("add", this::addFile);
        commandMap.put("delete", this::deleteFile);
        commandMap.put("get", this::getFile);
        commandMap.put("list-files", this::listFile);

        return commandMap;
    }

    private String addFile(String userInput) {
        String[] tokens = userInput.split(" ");

        String cloudPath = tokens[2];
        Path filePath = Paths.get(tokens[1]).toAbsolutePath().normalize();
        int numOfChunks = FileChunker.getChunkNumber(filePath);
        long fileSize = FileChunker.getFileSize(filePath);

        this.intermediateFilePaths.put(cloudPath, filePath.toString());

        request(new ClientRequestsFileAdd(cloudPath, numOfChunks, fileSize));

        return String.format("Add file: '%s', local: %s, size: %d, chunks: %d.", cloudPath, filePath, fileSize, numOfChunks);
    }

    private String deleteFile(String userInput) {
        String[] tokens = userInput.split(" ");
        String fileName = "";
        request(new ClientRequestsFileDelete(tokens[1]));
        return String.format("Delete file: '%s'.", fileName);
    }

    private String getFile(String userInput) {
        String fileName = "";
        String[] tokens = userInput.split(" ");
        request(new ClientRequestsFile(tokens[1]));

        return String.format("Get file: '%s'.", fileName);
    }

    private String listFile(String userInput) {
        String[] tokens = userInput.split(" ");
        request(new ClientRequestsFileList(tokens[0]));

        return "Retrieving File-list metadata.";
    }

    private void request(Event event) {
        try {
            if (controllerSocket == null) {

                controllerSocket = new Socket(controllerHost, controllerPort);
                SocketStream ss = new SocketStream(controllerSocket);
                connectionHandler.addConnection(ss);

                //create a listener on this new connection to listen for future responses
                Thread receiver = new Thread(new TCPReceiver(this, ss, server));
                receiver.start();
            }
            logger.debug("Sending request");
            //send it off to the Controller to respond
            sendMessage(controllerSocket, event);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
