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
    private final Event eventToSend;

    public TCPSender(Socket socket, Event event) {
        this.socket = socket;
        this.eventToSend = event;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());

        //use known socket connection to send data
        try {
            ObjectOutputStream dataOut = new ObjectOutputStream(socket.getOutputStream());

            //synchronize access so multiple threads don't attempt to write and corrupt the message
            synchronized (socket) {
                dataOut.writeObject(eventToSend);
                dataOut.flush();
            }
        } catch (SocketException se) {
            logger.error(socket);
            se.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
