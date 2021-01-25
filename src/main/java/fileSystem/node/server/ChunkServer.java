package fileSystem.node.server;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.transport.TCPServer;
import fileSystem.util.ConsoleParser;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChunkServer extends Node {

    private final int controllerPort;
    private final String name;

    //needed to display, not the most DRY
    final String[] commandList = {"list-files", ""};

    public ChunkServer(int portConnect, String name) {
        this.controllerPort = portConnect;
        this.name = name;
    }

    public static void main(String[] args) throws UnknownHostException {
        final int portListen = Integer.parseInt(args[0]);
        final int portConnect = Integer.parseInt(args[1]);
        final String name = args[2];

        //random port
        //int port = 0;

        //get the hostname and IP address
        InetAddress ip = InetAddress.getLocalHost();
        String host = ip.getHostName();
        System.out.printf("IP: %s, Host: %s%n", ip.getHostAddress(), host);

        //get an object reference to be able to call functions and organize control flow
        ChunkServer server = new ChunkServer(portConnect, name);

        //create a server thread to listen to incoming connections
        Thread tcpServer = new Thread(new TCPServer(server, portListen));
        tcpServer.start();

        //create the console, this may not be needed for the chunkServer, but could be useful for debugging
        Thread console = new Thread(new ConsoleParser(server));
        console.start();
    }

    private void showConfig() {
        System.out.printf("ServerName: '%s', ControllerPort: '%s'%n", name, controllerPort);
    }

    @Override
    public void onEvent(Event e, Socket socket) {

    }

    @Override
    public boolean handleCommand(String input) {
        boolean isValid = true;
        switch (input) {
            case "list-files":
                break;
            case "config":
                showConfig();
                break;
            default:
                isValid = false;
        }
        return isValid;
    }

    @Override
    protected String getHelp() {
        return "This is strictly used for development and to see system details locally. " +
                "Available commands are shown with 'commands'.";
    }

    @Override
    protected String getIntro() {
        return "Distributed System ChunkServer (DEV ONLY), type " +
                "'help for more details': ";
    }

    @Override
    public String[] getCommands() {
        return commandList;
    }

    @Override
    public void cleanup() {
        server.cleanup();
    }
}
