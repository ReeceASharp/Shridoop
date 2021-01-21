package fileSystem.node.client;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;

import java.net.Socket;

public class Client extends Node {

    String[] commandList = {"", ""};

    @Override
    public void handleCommand(String input) {

    }

    @Override
    public void onEvent(Event e, Socket socket) {

    }

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
    protected String getHelp() {
        return "Client Help";
    }

    @Override
    protected String getIntro() {
        return "Client Intro";
    }

    @Override
    public String[] getCommands() {
        return commandList;
    }

    @Override
    public void cleanup() {

    }
}
