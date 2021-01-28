package fileSystem.transport;

import fileSystem.node.Node;
import fileSystem.protocols.Event;
import fileSystem.protocols.EventFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
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
        // add reference to TCPServer to allow it to keep track of all current receiving threads for
        // this node and clean them up when exiting
        server.addConnection(this);

        //temporary values for holding message data
        int dataLength;
        byte[] incomingMessage;
        DataInputStream dataIn = null;

        while (!socket.isClosed()) {
            try {
                //wrap input stream to leverage better methods
                dataIn = new DataInputStream(socket.getInputStream());

                //first read data will always be the size of the data
                //NOTE: this line blocks
                dataLength = dataIn.readInt();

                //synchronize reads from a socket to make sure it's all read in chunks
                synchronized (socket) {
                    incomingMessage = new byte[dataLength];
                    dataIn.readFully(incomingMessage, 0, dataLength);
                }

                //convert to appropriate event type, and pass it along to the receiving node to handle
                Event e = EventFactory.getInstance().createEvent(incomingMessage);
                node.onEvent(e, socket);

            } catch (SocketException se) {
                //TODO: implement logger
                System.out.println("TCPReceiver::run::socketException: " + se.getMessage());
                break;
            } catch (IOException ioe) {
                System.out.println("Connection closed, no longer listening to: " + socket.getRemoteSocketAddress());
                break;
            } catch (NullPointerException ne) {
                ne.printStackTrace();
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
        logger.debug("CLEANING UP: " + socket);
            if (!socket.isClosed())
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.error(socket);
                }
            //The socket/connection is closed, remove the reference from the list
            //server.removeConnection(this);
    }

}
