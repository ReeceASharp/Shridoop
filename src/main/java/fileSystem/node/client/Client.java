package fileSystem.node.client;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.protocols.events.ClientRequest;
import fileSystem.protocols.events.ControllerReportsFileMetadata;
import fileSystem.transport.TCPReceiver;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import static fileSystem.protocols.Protocol.*;

public class Client extends Node {
    private static final Logger logger = LogManager.getLogger(Client.class);
    final String[] commandList = {"add", "delete", "get", "list-files"};
    //used by set, saves the hassle of putting the host:port into every request
    String controllerHost;
    int controllerPort;

    public Client(String host, int port) {
        this.controllerHost = host;
        this.controllerPort = port;
    }


    public static void main(String[] args) {
        //TODO: parse inputs and setup TCP connection

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(host, port);

        //create a server thread to listen to incoming connections
        Semaphore setupLock = new Semaphore(1);
        setupLock.tryAcquire();
        Thread tcpServer = new Thread(new TCPServer(client, 0, setupLock));
        tcpServer.start();

        //Console parser
        Thread console = new Thread(new ConsoleParser(client));
        console.start();

        //TODO: send connection, or use console

    }

    @Override
    public boolean handleCommand(String input) {
        boolean isValid = true;
        String[] tokens = input.split(" ");

        //TODO: break up into different methods, shouldn't clean, and organize flow
        int startIndex = controllerHost == null ? 2 : 0;

        if (startIndex == 2) {
            controllerHost = tokens[0];
            controllerPort = Integer.parseInt(tokens[1]);
        }

        String[] standardizedString = Arrays.copyOfRange(tokens, startIndex, tokens.length + 1);

        switch (tokens[startIndex]) {
            case "add":
                request(standardizedString, REQUEST_ADD);
                break;
            case "delete":
                request(standardizedString, REQUEST_DELETE);
                break;
            case "get":
                request(standardizedString, REQUEST_GET);
                break;
            case "list-files":
                request(standardizedString, REQUEST_FILE_LIST);
                break;
            default:
                isValid = false;
        }

        return isValid;
    }

    private void request(String[] input, int requestType) {

        try {
            Socket socket = new Socket(controllerHost, controllerPort);

            //create a listener on this new connection to listen for future responses
            Thread receiver = new Thread(new TCPReceiver(this, socket, server));
            receiver.start();

            //create the event and send it off to the Controller to respond
            Event event = new ClientRequest(requestType, input[1]);
            sendMessage(socket, event);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(Event e, Socket socket) {
        switch (e.getType()) {
            // Controller -> Client
            case CONTROLLER_REPORTS_CLIENT_REQUEST_STATUS:
                break;
            case CONTROLLER_REPORTS_CHUNK_SERVER_METADATA:
                break;
            case CONTROLLER_REPORTS_FILE_METADATA:
                displayFiles(e);
                break;

            // ChunkServer -> Client
            case CHUNK_SERVER_SENDS_FILE_CHUNK:
                break;
        }

    }

    private void displayFiles(Event e) {
        ControllerReportsFileMetadata response = (ControllerReportsFileMetadata) e;

        //TODO: get a list of files, display formatted information

    }

    @Override
    public void cleanup() {
        server.cleanup();
    }

    @Override
    protected String getHelp() {
        return "Client: This is the interface that is used to connect to a currently running Controller.";
    }

    @Override
    protected String getIntro() {
        return "Distributed System Client: Connect to a known cluster with the 'connect [IP:PORT]' command. " +
                "More information available via 'help'.";
    }

    @Override
    public String[] getCommands() {
        return commandList;
    }

}
