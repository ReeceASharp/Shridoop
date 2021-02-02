package fileSystem.transport;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/*
This packages a thread around the socket to grab the information being received, then handles the request
 */
public class TCPReceiver implements Runnable {
    private static final Logger logger = LogManager.getLogger(TCPReceiver.class);

    private final Node node;
    private final Socket socket;
    private final TCPServer server;

    public TCPReceiver(Node node, Socket socket, TCPServer server) {
        this.node = node;
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());

        // add reference to TCPServer to allow it to keep track of all current receiving threads for
        // this node and clean them up when exiting
        server.addConnection(this);

        //temporary values for holding message data
        Event event = null;
        ObjectInputStream dataIn = null;

        while (!socket.isClosed()) {
            try {
                //wrap input stream to leverage better methods
                InputStream ios = socket.getInputStream();

                dataIn = new ObjectInputStream(ios);

                //synchronize reads from a socket to make sure it's all read in chunks
                synchronized (socket) {
                    event = (Event) dataIn.readObject();
                }
                //convert to appropriate event type, and pass it along to the receiving node to handle
                //Event e = EventFactory.getInstance().createEvent(incomingMessage);
                node.onEvent(event, socket);

            } catch (EOFException eof) {
                // standard exit method, means that the other side closed off its socket, causing this side's socket
                // to throw this error, which follows the ControllerReportsShutdown control flow
                logger.debug("Closing up the connection. Proper exit.");
                cleanup();
            } catch (SocketException se) {
                //TODO: implement logger
                logger.error(se.getMessage() + ", " + socket);
                cleanup();
            } catch (IOException ioe) {
                logger.error("IOException: Connection closed, no longer listening to: " + socket);
                ioe.printStackTrace();
                cleanup();
            } catch (NullPointerException ne) {
                ne.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        //attempt to close stream given that the socket has now been closed for some reason
        if (dataIn != null)
            try {
                dataIn.close();
            } catch (IOException e) {
                System.out.println("Attempting to close Pipes");
                e.printStackTrace();
            }
    }

    public void cleanup() {
        // needs to be synchronized as cleanup is a multi-step process, if it only partially runs before something
        // else attempts to use/modify it, the TCPReceiver is left in an unsafe state
        synchronized (socket) {
            if (!socket.isClosed())
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.error(socket);
                }
        }
    }

}
