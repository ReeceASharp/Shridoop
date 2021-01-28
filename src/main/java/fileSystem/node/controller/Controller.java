package fileSystem.node.controller;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.protocols.events.*;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static fileSystem.protocols.Protocol.*;

public class Controller extends Node {
    private static final Logger logger = LogManager.getLogger(Controller.class);

    private static final String[] commandList = {"list-nodes", "list-files", "init", "stop", "display-config"};
    private final int port;
    private final ArrayList<ChunkData> chunkServerList;
    private boolean isActive;

    private CountDownLatch activeChunkServers;

    public Controller(int port) {
        isActive = false;
        this.port = port;
        chunkServerList = new ArrayList<>();
    }

    public static void main(String[] args) throws UnknownHostException {

        boolean DEBUG = true;

        if (DEBUG) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            loggerConfig.setLevel(Level.ALL);
            ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.
        }

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
            case "stop":
                stopChunkServers();
                break;
            case "display-config":
                showConfig();
                break;
            default:
                isValid = false;
        }
        return isValid;
    }

    private void stopChunkServers() {
        if (!isActive)
            return;
        isActive = false;
        //Request that each server shutdown

        try {
            synchronized (chunkServerList) {
                activeChunkServers = new CountDownLatch(chunkServerList.size());
                logger.info(String.format("Sending shutdown request to %d nodes.", activeChunkServers.getCount()));

                for (ChunkData chunkServer : chunkServerList) {
                    byte[] marshalledBytes = new ControllerRequestsDeregistration().getBytes();
                    sendMessage(chunkServer.socket, marshalledBytes);
                }

            }
            logger.debug("Waiting until all servers have exited");
            activeChunkServers.await();
            logger.debug("All servers have responded, exiting");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException ne) {
            synchronized (chunkServerList) {
                logger.debug("Size: " + chunkServerList.size());
                for (ChunkData chunkServer : chunkServerList) {
                    logger.debug(chunkServer);
                }
            }
        }
    }

    @Override
    public void onEvent(Event e, Socket socket) {
        switch (e.getType()) {
            // ChunkServer -> Controller
            case CHUNK_SERVER_REQUESTS_REGISTRATION:
                chunkServerRegistration(e, socket);
                break;
            case CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS:
                deregistrationResponse(e, socket);
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

    private void deregistrationResponse(Event e, Socket socket) {
        ChunkServerReportsDeregistrationStatus response = (ChunkServerReportsDeregistrationStatus) e;

        //create the relevant object to find using the overloaded equals operator for ChunkData
        boolean removed = chunkServerList.remove(new ChunkData(response.getName(),
                response.getIP(), response.getPort(), socket));

        if (removed)
            logger.info(String.format("%s successfully showdown.", response.getName()));
        else
            logger.error(String.format("%s unsuccessfully showdown.", response.getName()));

        // confirm the shutdown request
        byte[] marshalledBytes = new ControllerReportsShutdown().getBytes();
        sendMessage(socket, marshalledBytes);

        //TODO: fix race condition
        activeChunkServers.countDown();
    }

    private void chunkServerRegistration(Event e, Socket socket) {
        ChunkServerRequestsRegistration request = (ChunkServerRequestsRegistration) e;
        ChunkData temp = new ChunkData(request.getName(), request.getIP(), request.getPort(), socket);

        synchronized (chunkServerList) {
            chunkServerList.add(temp);
        }

        logger.debug("Received Registration Request: " + temp);

        //TODO: respond
        byte[] marshalledBytes = new ControllerReportsRegistrationStatus(RESPONSE_SUCCESS).getBytes();

        logger.debug("Responding to request on socket: " + socket.getLocalSocketAddress());
        sendMessage(socket, marshalledBytes);

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
        if(isActive)
            stopChunkServers();

        server.cleanup();
    }

    /**
     * Returns all information currently loaded in regarding the configuration of the cluster
     */
    public void showConfig() {
        System.out.println("**** NODES ****");
        System.out.println("Nodes: " + chunkServerList.size());
        for (ChunkData server : chunkServerList)
            System.out.println(server);
        System.out.println(" ************* ");
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

            //FIXME: This code chunk blocks, which blocks subsequent commands from being entered
//            //get the output from the script, which includes just the information
//            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            StringBuilder builder = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                builder.append(line);
//                builder.append(System.getProperty("line.separator"));
//            }
//            System.out.println(builder.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
