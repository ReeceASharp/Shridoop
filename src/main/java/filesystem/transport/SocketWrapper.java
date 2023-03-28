package filesystem.transport;

import filesystem.interfaces.Event;
import filesystem.interfaces.EventInterface;
import filesystem.util.HostPortAddress;
import filesystem.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketWrapper implements Runnable {
    private static final Logger logger = LogManager.getLogger(SocketWrapper.class);
    public final Socket socket;
    public final HostPortAddress address;
    //    The server interface is used to pass the event to the node
    private final EventInterface eventInterface;
    public ObjectOutputStream outStream;
    public ObjectInputStream inStream;
//    TODO: Temp naming schema to dictate where the connections are being created
//    Or do some better logging


    public SocketWrapper(Socket socket, EventInterface eventInterface) throws IOException {
        this.socket = socket;
        this.address = new HostPortAddress(socket.getLocalAddress().getCanonicalHostName(), socket.getPort());

        this.eventInterface = eventInterface;

        // DEAR GOD OUT BEFORE IN (2023 Reece, I don't recall why I wrote this, but it scares me)
        this.outStream = new ObjectOutputStream(socket.getOutputStream());
        this.inStream = new ObjectInputStream(socket.getInputStream());
        this.outStream.flush();

    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());

        Event event = null;
        while (!socket.isClosed()) {
            try {
                try {
                    event = receiveEvent();
                    if (event == null) continue;
                } catch (Exception e) {
                    // Always try to clean-up on exception, then handle it separately
                    cleanup();
                    // TODO: Make a custom Exception with the socket and address so that
                    //  wrapping classes can handle this more gracefully
                    throw e;
                }

            } catch (EOFException eof) {
                // standard exit method, means that the other side closed off its socket, causing this side's socket
                // to throw this error, which follows the ControllerReportsShutdown control flow
                logger.debug("Closing up the connection. Proper exit.");
            } catch (IOException ioe) {
                logger.error("ERROR: Connection closed, no longer listening to: " + socket);
            } catch (ClassNotFoundException e) {
                logger.error("Received Invalid Event. Ignoring.");
            }

            if (Boolean.getBoolean(Properties.get("MESSAGE_DEBUG"))) logger.debug("Received Message: " + event);

            // Pass it off to the callback
            eventInterface.handleEvent(event, socket);
        }
        logger.debug("Closing socket: " + socket);
    }


    @Override
    public String toString() {
        return "SocketStream{" + "socket=" + socket + ", outStream=" + outStream + ", inStream=" + inStream + '}';
    }

    public void cleanup() throws IOException {
        inStream.close();
        outStream.close();
        socket.close();
    }

    public synchronized Event receiveEvent() throws IOException, ClassNotFoundException {
        return (Event) inStream.readObject();
    }
}
