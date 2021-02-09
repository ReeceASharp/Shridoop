package fileSystem.transport;

import fileSystem.node.controller.Controller;
import fileSystem.protocols.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Organizes how marshalled data is sent
 */
public class TCPSender implements Runnable {
    private static final Logger logger = LogManager.getLogger(Controller.class);

    private final Socket socket;
    private final ObjectOutputStream outStream;
    private final Event eventToSend;

    public TCPSender(SocketStream socketStream, Event event) {
        this.socket = socketStream.socket;
        this.outStream = socketStream.outStream;
        this.eventToSend = event;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());
        try {
            //synchronize access so multiple threads don't attempt to write and corrupt the message
                //logger.debug("SENDING MESSAGE" + socket);

            outStream.writeObject(eventToSend);
            outStream.flush();

        } catch (SocketException se) {
            logger.error(socket);
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
