package filesystem.transport;

import filesystem.node.Node;
import filesystem.protocol.Event;
import filesystem.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;

/*
This packages a thread around the socket to grab the information being received, then passes the request off.
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
        try {
            socketStream.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Event event = null;
        while (!socketStream.socket.isClosed()) {
            try {

                try {
                    event = socketStream.receiveEvent();
                    if (event == null)
                        continue;
                } catch (Exception e) {
                    // Always try to cleanup on exception, then handle it separately
                    cleanup();
                    throw e;
                }

            } catch (EOFException eof) {
                // standard exit method, means that the other side closed off its socket, causing this side's socket
                // to throw this error, which follows the ControllerReportsShutdown control flow
                logger.debug("Closing up the connection. Proper exit.");
            } catch (IOException ioe) {
                logger.error("ERROR: Connection closed, no longer listening to: " + socketStream.socket);
            } catch (ClassNotFoundException e) {
                logger.error("Received Invalid Event. Ignoring.");
            }

            if (Boolean.getBoolean(Properties.get("MESSAGE_DEBUG")))
                logger.debug("Received Message: " + event);
            node.onEvent(event, socketStream.socket);
        }
        logger.debug("Closing socket: " + socketStream.socket);
    }


    public void cleanup() {
        try {
            socketStream.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.removeConnection(this);
        node.onLostConnection(this.socketStream.socket);
    }

}
