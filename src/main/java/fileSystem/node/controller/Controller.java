package fileSystem.node.controller;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Controller extends Node {

    //TODO: build a ChunkServer information datastructure

    String[] commandList = {"list-nodes", ""};

    @Override
    public void handleCommand(String input) {

        switch(input) {
            case "list-nodes":
                break;

        }

    }

    public Controller() {

    }

    @Override
    public void onEvent(Event e, Socket socket) {

    }

    public static void main(String[] args) throws UnknownHostException {

        int port = 5000;

        //get the hostname and IP address
        InetAddress ip = InetAddress.getLocalHost();
        String host = ip.getHostName();
        System.out.printf("IP: %s, Host: %s%n", ip.getHostAddress(), host);

        Controller controller = new Controller();

        //create a server thread to listen to incoming connections
        Thread tcpServer = new Thread(new TCPServer(controller, port));
        tcpServer.start();

        //create the console
        Thread console = new Thread(new ConsoleParser(controller));
        console.start();

    }

    @Override
    protected String getHelp() {
        return "Controller Help";
    }

    @Override
    protected String getIntro() {
        return "Controller Intro";
    }

    @Override
    public String[] getCommands() {
        return commandList;
    }


    @Override
    public void cleanup() {
        System.exit(0);
    }
}
