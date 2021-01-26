package fileSystem.node.controller;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.protocols.events.ChunkServerSendsRegistration;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static fileSystem.protocols.Protocol.*;

public class Controller extends Node {
    private final int port;
    private static final String[] commandList = {"list-nodes", "list-files", "init", "display-config"};
    private boolean isActive;

    private final ArrayList<ChunkData> chunkServerList;

    public Controller(int port) {
        isActive = false;
        this.port = port;
        chunkServerList = new ArrayList<>();
    }

    public static void main(String[] args) throws UnknownHostException {

        int port = 5000;
        //get the hostname and IP address
        InetAddress ip = InetAddress.getLocalHost();
        String host = ip.getHostName();
        System.out.printf("[CONTROLLER] IP: %s, Host: %s%n", ip.getHostAddress(), host);

        Controller controller = new Controller(port);

        //create a server thread to listen to incoming connections
        Thread tcpServer = new Thread(new TCPServer(controller, port));
        tcpServer.start();

        //create the console
        Thread console = new Thread(new ConsoleParser(controller));
        console.start();

    }

    @Override
    public boolean handleCommand(String input) {
        boolean isValid = true;
        switch (input) {
            case "list-nodes":
                //TODO
                break;
            case "list-files":
                //TODO
                break;
            case "init":
            case "initialize":
                initialize();
                break;
            case "display-config":
                showConfig();
                break;
            default:
                isValid = false;
        }
        return isValid;
    }

    @Override
    public void onEvent(Event e, Socket socket) {
        switch(e.getType()) {
            // ChunkServer -> Controller
            case CHUNK_SERVER_SENDS_REGISTRATION:
                chunkServerRegistration(e, socket);
                break;
            case CHUNK_SERVER_REPORTS_MAJOR_HEARTBEAT:
                break;
            case CHUNK_SERVER_REPORTS_MINOR_HEARTBEAT:
                break;
            case CHUNK_SERVER_REPORTS_FILE_CHUNK_METADATA:
                break;

            // Client -> Controller
            case CLIENT_REQUESTS_FILE_SAVE:
                break;
            case CLIENT_REQUESTS_FILE:
                break;
            case CLIENT_REQUESTS_FILE_DELETE:
                break;
            case CLIENT_REQUESTS_CHUNK_SERVER_METADATA:
                break;
            case CLIENT_REQUESTS_FILE_METADATA:
                break;
        }
    }

    private void chunkServerRegistration(Event e, Socket socket) {
        ChunkServerSendsRegistration request = (ChunkServerSendsRegistration) e;
        chunkServerList.add(new ChunkData(request.getName(), request.getIP(), request.getPort()));

        //TODO: respond
    }

    @Override
    protected String getHelp() {
        return "Controller: This is the driver that organizes all communication for" +
                "the cluster. Setup of the cluster is done here, and clients all communicate " +
                "with this driver in order to store, update, or remove files. Available commands " +
                "are shown with 'commands'.";
    }

    @Override
    protected String getIntro() {
        return "Distributed System Controller: Enter 'help' for more information on configuration " +
                "or 'init' to start up the cluster";
    }

    @Override
    public String[] getCommands() {
        return commandList;
    }


    @Override
    public void cleanup() {
        //TODO: Tell all ChunkServers to exit, requires API
        server.cleanup();

        // Ugly exit method, but exits out of threads that we don't have access to anymore (TCPServer)
        // Note: this could be refactored to maintain a reference to circumvent this, but that might be too jank
        //System.exit(0);
    }

    /**
     * Returns all information currently loaded in regarding the configuration of the cluster
     */
    public void showConfig() {
        System.out.println();
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
        // running the servers, this hard-codes it to a windows enviroment at the moment
        try {
            ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\Git\\bin\\bash.exe",
                    "-c", "bash ./start_chunks.sh " + port);
            Process p = pb.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
