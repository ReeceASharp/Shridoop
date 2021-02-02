package fileSystem.node.controller;

import fileSystem.node.Heartbeat;
import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.protocols.events.*;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;
import fileSystem.util.HeartbeatHandler;
import fileSystem.util.LogConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static fileSystem.protocols.Protocol.*;

public class Controller extends Node implements Heartbeat {
    private static final Logger logger = LogManager.getLogger(Controller.class);

    //properties
    private static final String[] commandList = {"list-nodes", "list-files", "init", "stop", "display-config"};
    private final int port;
    private final ArrayList<ServerData> chunkServerList;

    //control flow
    private boolean isActive;
    private HeartbeatHandler timer;

    // Synchronization for startup and shutdown
    private CountDownLatch activeChunkServers;

    public Controller(int port) {
        isActive = false;
        this.port = port;
        chunkServerList = new ArrayList<>();
    }

    public static void main(String[] args) throws UnknownHostException {

        // Inputs (Eventually)
        boolean DEBUG = true;
        int port = 5000;

        //setup debug mode logging if parameter was passed
        if (DEBUG)
            LogConfiguration.debug();


        //get the hostname and IP address
        InetAddress ip = InetAddress.getLocalHost();
        String host = ip.getHostName();

        logger.info(String.format("IP: %s, Host: %s%n", ip.getHostAddress(), host));

        Controller controller = new Controller(port);
        controller.configure();

        // create a server thread to listen to incoming connections, the semaphore isn't currently used here, only in
        // the ChunkServer, but will if the controller needs to send off a connection upon startup

        Thread tcpServer = new Thread(new TCPServer(controller, port, null));
        tcpServer.start();

        //create the console
        Thread console = new Thread(new ConsoleParser(controller));
        console.start();
    }

    @Override
    public void onHeartBeat(int type) {
        //send out requests to each of the current ChunkServers to make sure no failures have occurred
        Event e = new ControllerRequestsFunctionalHeartbeat();

        for (ServerData cd : chunkServerList) {
            sendMessage(cd.socket, e);
        }
    }

    private void configure() {
        // Note: as this class uses a reference to the controller, it must be constructed after the Controller
        // constructor to make sure it's fully setup
        timer = new HeartbeatHandler(5, 9, this);
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
        if (!isActive) {
            System.out.println("Cluster isn't currently active");
            return;
        }
        isActive = false;
        timer.stop();

        //Request that each server shutdown
        try {
            synchronized (chunkServerList) {
                activeChunkServers = new CountDownLatch(chunkServerList.size());
                logger.info(String.format("Sending shutdown request to %d nodes.", activeChunkServers.getCount()));

                Event event = new ControllerRequestsDeregistration();
                for (ServerData chunkServer : chunkServerList) {
                    sendMessage(chunkServer.socket, event);
                }

            }
            activeChunkServers.await();
            logger.debug("All servers have responded, exiting");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException ne) {
            synchronized (chunkServerList) {
                logger.debug("Size: " + chunkServerList.size());
                for (ServerData chunkServer : chunkServerList) {
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
            case CHUNK_SERVER_SENDS_MINOR_HEARTBEAT:
                break;
            case CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT:
                break;
            // Client -> Controller
            case CLIENT_REQUESTS_FILE_ADD:
                fileAdd(e, socket);
            case CLIENT_REQUESTS_FILE_DELETE:
                fileDelete(e, socket);
                break;
            case CLIENT_REQUESTS_FILE:
                fileGet(e, socket);
                break;
            case CLIENT_REQUESTS_FILE_LIST:
                fileList(e, socket);
                break;
            case CLIENT_REQUESTS_CHUNK_SERVER_METADATA:
                break;
        }

    }

    /**
     * Packages and sends off the current file information the Controller is keeping to the client
     * @param e  Event that contains a ClientRequestsFileList, contains an optional path parameter to search through
     * @param socket
     */
    private void fileList(Event e, Socket socket) {
        logger.debug("Controller received FileList request");
        ClientRequestsFileList request = (ClientRequestsFileList) e;

        //TODO: logic to handle getting the files in the system, or under the optional path
        ArrayList<String> files = new ArrayList<>();

        Event response = new ControllerReportsFileList(files);
        sendMessage(socket, response);
    }

    /**
     * Respond with a current list of ChunkServers containing all different chunks of the file
     *
     * @param e  The event containing the file the client wants
     * @param socket
     */
    private void fileGet(Event e, Socket socket) {
        // Look at current list of files in system, and respond with the list of servers associated with the requested
        // file, or respond with a negative status in the case of an absence of that file

        //TODO: Logic to check for file existence + get servers hosting chunks of said file

        Event response = new ControllerReportsServerContactList(RESPONSE_FAILURE, null);
        sendMessage(socket, response);
    }

    /**
     * Respond with a status as to whether the delete was successful, or unsuccessful (FileNotFound?)
     *
     * @param e the event containing the file that the client wants deleted
     * @param socket
     */
    private void fileDelete(Event e, Socket socket) {
        //See if file exists in the system, and send out requests to delete it, if it exists

        //TODO: Logic to check for file existence + get servers hosting chunks of said file

        Event response = new ControllerReportsServerContactList(RESPONSE_FAILURE, null);
        sendMessage(socket, response);
    }

    /**
     * Respond with a current list of ChunkServers to open a connection to, and send different chunks to
     *
     * @param e the event that contains the file being requested to be added
     * @param socket
     */
    private void fileAdd(Event e, Socket socket) {
        ClientRequestsFileAdd request = (ClientRequestsFileAdd) e;

        //TODO: logic to get a list of servers to pass back to the client for it to send the data to directly

        Event response = new ControllerReportsServerContactList(RESPONSE_FAILURE, null);
        sendMessage(socket, response);
    }

    private void deregistrationResponse(Event e, Socket socket) {
        ChunkServerReportsDeregistrationStatus response = (ChunkServerReportsDeregistrationStatus) e;

        //create the relevant object to find using the overloaded equals operator for ChunkData
        boolean removed;
        synchronized (chunkServerList) {
            removed = chunkServerList.remove(new ServerData(response.getName(),
                    response.getIP(), response.getPort(), socket));
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

    private void chunkServerRegistration(Event e, Socket socket) {
        ChunkServerRequestsRegistration request = (ChunkServerRequestsRegistration) e;
        ServerData temp = new ServerData(request.getName(), request.getIP(), request.getPort(), socket);

        synchronized (chunkServerList) {
            chunkServerList.add(temp);
        }

        logger.debug("Received Registration Request: " + temp);

        //TODO: respond
        Event event = new ControllerReportsRegistrationStatus(RESPONSE_SUCCESS);

        logger.debug("Responding to request on socket: " + socket.getLocalSocketAddress());
        sendMessage(socket, event);

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
        if (isActive)
            stopChunkServers();
        server.cleanup();
    }

    /**
     * Returns all information currently loaded in regarding the configuration of the cluster
     */
    public void showConfig() {
        System.out.println("**** NODES ****");
        System.out.println("Nodes: " + chunkServerList.size());
        for (ServerData server : chunkServerList)
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

        // As an alternative to the above blocking call, simply read the chunkServers file, and setup a
    }
}
