package fileSystem.node.client;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.protocols.events.*;
import fileSystem.transport.SocketStream;
import fileSystem.transport.TCPReceiver;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;
import fileSystem.util.ContactList;
import fileSystem.util.FileChunker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static fileSystem.protocols.Protocol.*;

public class Client extends Node {
    private static final Logger logger = LogManager.getLogger(Client.class);

    final String[] commandList = {"add", "delete", "get", "list-files"};
    private final Semaphore commandLock;
    private final String controllerHost;
    private final int controllerPort;
    //Temporary, until I figure out a better solution
    private String localFilePath;
    private Socket controllerSocket;


    public Client(String host, int port) {
        this.controllerHost = host;
        this.controllerPort = port;
        this.commandLock = new Semaphore(1);
    }


    public static void main(String[] args) {
        //TODO: parse inputs and setup TCP connection

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
    public boolean handleCommand(String input) {
        boolean isValid = true;
        String[] tokens = input.split(" ");

        switch (tokens[0]) {
            case "add":
                localFilePath = tokens[1];
                request(new ClientRequestsFileAdd(tokens[2],
                        FileChunker.getChunkNumber(tokens[1])));
                break;
            case "delete":
                request(new ClientRequestsFileDelete(tokens[1]));
                break;
            case "get":
                request(new ClientRequestsFile(tokens[1]));
                break;
            case "list-files":
                request(new ClientRequestsFileList(tokens[0]));
                break;
            default:
                isValid = false;
        }

        return isValid;
    }

    private void request(Event event) {
        try {
            //grab a lock
            commandLock.acquire();
            if (controllerSocket == null) {

                controllerSocket = new Socket(controllerHost, controllerPort);
                SocketStream ss = new SocketStream(controllerSocket);
                connectionHandler.addConnection(ss);

                //create a listener on this new connection to listen for future responses
                Thread receiver = new Thread(new TCPReceiver(this, ss, server));
                receiver.start();
            }

            //send it off to the Controller to respond
            sendMessage(controllerSocket, event);

            //block on message send, release on successful message response
            //Note: this could have problems if the response never comes for some reason
            commandLock.acquire();

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(Event e, Socket socket) {
        switch (e.getType()) {
            // Controller -> Client
            case CONTROLLER_REPORTS_FILE_LIST:
                displayFiles(e);
                break;
            case CONTROLLER_REPORTS_CHUNK_GET_LIST:
                getFileFromServers(e);
                break;
            case CONTROLLER_REPORTS_CHUNK_ADD_LIST:
                addFileToServers(e);
                break;
            case CONTROLLER_REPORTS_FILE_DELETE_STATUS:
                deleteStatus(e);
                break;

            // ChunkServer -> Client
            case CHUNK_SERVER_SENDS_FILE_CHUNK:
                break;
        }

    }

    /**
     * Displays whether the delete request went through
     *
     * @param e The event to be converted to ControllerReportsFileDeleteStatus
     */
    private void deleteStatus(Event e) {
        ControllerReportsFileDeleteStatus response = (ControllerReportsFileDeleteStatus) e;

        switch (response.getStatus()) {
            case RESPONSE_SUCCESS:
                break;
            case RESPONSE_FAILURE:
                break;

        }

        commandLock.release();
    }

    private void addFileToServers(Event e) {
        ControllerReportsChunkAddList response = (ControllerReportsChunkAddList) e;

        // Setup file input streams to handle the chunk creation of the file,
        // use try-with-resource to simplify the closing/cleanup of the input streams
        try (FileInputStream fis = new FileInputStream(localFilePath);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            int bytesRead;
            //maximum size of chunk
            byte[] buffer = new byte[CHUNK_SIZE];

            // Dynamically fetch data from the file in chunksize increments, we already calculated and passed the
            // # of chunks to the Controller, and it responded with a set of servers for each chunk to be sent to
            for (ContactList chunk : response.getChunkDestinations()) {
                // Get chunk data, and copy into a dynamically sized array to minimize chunk size
                // this is only really useful for the last chunk of a large file, or the only
                // chunk of a small file
                bytesRead = bis.read(buffer);
                byte[] bytesToSend = new byte[bytesRead];
                System.arraycopy(buffer, 0, bytesToSend, 0, bytesRead);

                // Get the server at the front, the list is generated in a random order at the Controller,
                // so there's no reason to use further randomness
                ArrayList<String> serverList = chunk.getServersToContact();
                String[] hostPort = serverList.get(0).split(":");

                // Remove it from the list as it's the one being contacted
                serverList.remove(0);

                // Try to open a new connection with the server
                Socket serverSocket;
                try {
                    // Open a connection with the chunk server
                    serverSocket = new Socket(hostPort[0], Integer.parseInt(hostPort[1]));
                } catch (IOException unknownHostException) {
                    unknownHostException.printStackTrace();
                    continue;
                }

                ClientSendsFileChunk chunkSend = new ClientSendsFileChunk(chunk.getChunkNumber(), bytesToSend, serverList);

                sendMessage(serverSocket, chunkSend);
            }
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }


        commandLock.release();
    }

    private void getFileFromServers(Event e) {
        ControllerReportsChunkGetList response = (ControllerReportsChunkGetList) e;

        //TODO: handle logic of querying the ChunkServers

        commandLock.release();
    }

    private void displayFiles(Event e) {
        ControllerReportsFileList response = (ControllerReportsFileList) e;

        //TODO: get a list of files, display formatted information
        if (response.getStatus() == RESPONSE_SUCCESS) {
            System.out.println("**** Files **** ");
            for (String file : response.getFiles()) {
                System.out.println(file);
            }
            System.out.println("  ************  ");
        } else if (response.getStatus() == RESPONSE_FAILURE) {
            System.out.println("Failure to handle list query.");
        }
        //Now that command passed, release
        commandLock.release();
    }

    @Override
    public void cleanup() {
        server.cleanup();
    }

    @Override
    protected String getHelp() {
        return "Client: This is the interface that is used to connect to a currently running Controller. Using one" +
                "of the commands [add,get,delete] and a file parameter to modify the information on the cluster.";
    }

    @Override
    protected String getIntro() {
        return "Distributed System Client: Used to connect to a Controller, can 'get', 'add', and 'delete' files from" +
                "the cluster. More information available via 'help'.";
    }

    @Override
    public String[] getCommands() {
        return commandList;
    }

}
