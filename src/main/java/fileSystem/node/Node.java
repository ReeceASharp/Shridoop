package fileSystem.node;

import fileSystem.protocols.Event;
import fileSystem.transport.TCPServer;

import java.net.Socket;
import java.util.Arrays;

import static fileSystem.util.ConsoleConstant.*;

/**
 * Abstraction of a node, which each part of the system is build upon
 */
public abstract class Node {
    //reference to the server
    protected TCPServer server;

    /**
     * Is passed the command and returns an output to the console
     * Note: Java 8 doesn't allow private methods in an interface, only in Java 9
     * As a result this must be an abstract class
     *
     * @param type text type to return
     * @return
     */
    public String getConsoleText(int type) {
        switch (type) {
            case CONSOLE_INTRO:
                return getIntro();
            case CONSOLE_HELP:
                return getHelp();
            case CONSOLE_COMMANDS:
                return Arrays.toString(getCommands());
        }
        // TODO: Throw error?
        return "ERROR";
    }

    /**
     * information displayed on console help request
     *
     * @return a string containing instructions on how to use the program
     */
    protected abstract String getHelp();

    /**
     * information displayed on startup
     *
     * @return a string containing a basic description of the node
     */
    protected abstract String getIntro();

    //get list of commands specific to a subclassed node
    public abstract String[] getCommands();

    /**
     * If this is a command specific to a subclassed node, do something with it
     *
     * @param input command to be executed
     * @return returns whether it was a valid command
     */
    public abstract boolean handleCommand(String input);

    /**
     * When receiving a command from a given TCP thread, do something with the request
     *
     * @param e      event to be decoded and handled
     * @param socket pipeline that the event came on in
     */
    public abstract void onEvent(Event e, Socket socket);

    /**
     * Called by the consoleParser to quit gracefully
     */
    public abstract void cleanup();

    /**
     * Set a reference inside of the node to the relating TCPServer. Benefit is it allows a graceful exit, and
     * can be used to get config information more easily, instead of it being stored redundantly inside the node
     *
     * @param ref a reference to the TCPServer
     */
    public void setTCPServer(TCPServer ref) {
        this.server = ref;
    }

}
