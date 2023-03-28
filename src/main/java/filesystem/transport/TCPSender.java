package filesystem.transport;

import filesystem.interfaces.Event;
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
    private static final Logger logger = LogManager.getLogger(TCPSender.class);

    private final Socket socket;
    private final ObjectOutputStream outStream;
    private final Event eventToSend;

    public TCPSender(SocketWrapper socketWrapper, Event event) {
        this.socket = socketWrapper.socket;
        this.outStream = socketWrapper.outStream;
        this.eventToSend = event;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());
        try {
            synchronized (outStream) {
                outStream.writeObject(eventToSend);
                outStream.flush();
            }
        } catch (SocketException se) {
            logger.error(socket);
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
