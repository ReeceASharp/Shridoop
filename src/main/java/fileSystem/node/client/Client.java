package fileSystem.node.client;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;

import java.net.Socket;

public class Client extends Node {

    final String[] commandList = {"connect", "add", "remove"};

    public Client() {

    }

    public static void main(String[] args) {
        //TODO: parse inputs and setup TCP connection
        int port = 0;

        Client client = new Client();

        //create a server thread to listen to incoming connections
        Thread tcpServer = new Thread(new TCPServer(client, port));
        tcpServer.start();

        //Console parser
        Thread console = new Thread(new ConsoleParser(client));
        console.start();

    }

    @Override
    public boolean handleCommand(String input) {
        boolean isValid = true;

        switch (input) {
            case "connect":
                connect();
                break;
            case "add":
                sendRequest("add");
                break;
            case "remove":
                sendRequest("remove");
                break;
            default:
                isValid = false;
        }

        return isValid;
    }

    private void connect() {

    }

    private void sendRequest(String requestType) {
        switch(requestType) {
            case "add":
                break;
            case "remove":
                break;
        }
    }

    @Override
    public void onEvent(Event e, Socket socket) {

    }

    @Override
    public void cleanup() {
        server.cleanup();
    }

    @Override
    protected String getHelp() {
        return "Client: This is the interface that is used to connect to a currently running Controller. ";
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
