package fileSystem.node.server;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChunkServer extends Node {

    String[] commandList = {"list-files", ""};

    @Override
    public void onEvent(Event e, Socket socket) {

    }

    @Override
    public void handleCommand(String input) {

    }

    public ChunkServer() {

    }

    public static void main(String[] args) throws UnknownHostException {

        int port = 0;

        //get the hostname and IP address
        InetAddress ip = InetAddress.getLocalHost();
        String host = ip.getHostName();
        System.out.printf("IP: %s, Host: %s%n", ip.getHostAddress(), host);

        //get an object reference to be able to call functions and organize control flow
        ChunkServer server = new ChunkServer();

        //create a server thread to listen to incoming connections
        Thread tcpServer = new Thread(new TCPServer(server, port));
        tcpServer.start();

        //create the console, this may not be needed for the chunkServer, but could be useful for debugging
        Thread console = new Thread(new ConsoleParser(server));
        console.start();
    }

    @Override
    protected String getHelp() {
        return "ChunkServer Help";
    }

    @Override
    protected String getIntro() {
        return "ChunkServer Intro";
    }

    @Override
    public String[] getCommands() {
        return commandList;
    }

    @Override
    public void cleanup() {

    }
}
