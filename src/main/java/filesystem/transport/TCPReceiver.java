package filesystem.transport;

import filesystem.node.Node;
import filesystem.protocol.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.SocketException;

/*
This packages a thread around the socket to grab the information being received, then handles the request
 */
public class TCPReceiver implements Runnable {
    private static final Logger logger = LogManager.getLogger(TCPReceiver.class);

    private final Node node;
    private final SocketStream socketStream;
    private final TCPServer server;

    public TCPReceiver(Node node, SocketStream socketStream, TCPServer server) {
        this.node = node;
        this.socketStream = socketStream;
        this.server = server;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());
        // add reference to TCPServer to allow it to keep track of all current receiving threads for
        // this node and clean them up when exiting
        //server.addConnection(this);

        //temporary values for holding message data
        Event event;
        //Socket socket = socketStream.socket;
        ObjectInputStream inStream = null;
        try {
            //create the inputstream, and update the reference
            inStream = new ObjectInputStream(socketStream.socket.getInputStream());
            socketStream.inStream = inStream;
        } catch (IOException e) {
            e.printStackTrace();
        }

        //logger.debug("LISTENING ON: " + socketStream);
        while (!socketStream.socket.isClosed()) {
            try {
                try {
                    synchronized (socketStream.socket) {
                        event = (Event) inStream.readObject();
                    }
                    logger.debug("Received Message: " + event.toString());
                    node.onEvent(event, socketStream.socket);
                } catch (Exception e) {
                    cleanup();
                    throw e;
                }
            } catch (EOFException eof) {
                // standard exit method, means that the other side closed off its socket, causing this side's socket
                // to throw this error, which follows the ControllerReportsShutdown control flow
                logger.debug("Closing up the connection. Proper exit.");
            } catch (SocketException se) {
                logger.error(se.getMessage() + ", " + socketStream.socket);
            } catch (IOException ioe) {
                logger.error("ERROR: Connection closed, no longer listening to: " + socketStream.socket);
                ioe.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        logger.debug("Exiting: " + socketStream.socket);

    }

    public void cleanup() {
        // needs to be synchronized as cleanup is a multi-step process, if it only partially runs before something
        // else attempts to use/modify it, the TCPReceiver is left in an unsafe state
        socketStream.cleanup();
        server.removeConnection(this);
        node.onLostConnection(this.socketStream.socket);
    }

}
