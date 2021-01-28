package fileSystem.transport;

import fileSystem.node.controller.Controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Organizes how marshalled data is sent
 */
public class TCPSender implements Runnable {
    private static final Logger logger = LogManager.getLogger(Controller.class);

    private final Socket socket;
    private final byte[] dataToSend;

    public TCPSender(Socket socket, byte[] data) {
        this.socket = socket;
        this.dataToSend = data;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());

        //use known socket connection to send data
        int dataLength = dataToSend.length;
        try {
            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

            //synchronize access so multiple threads don't attempt to write and corrupt the message
            synchronized (socket) {
                dataOut.writeInt(dataLength);
                dataOut.write(dataToSend, 0, dataLength);
                dataOut.flush();
            }
        } catch (SocketException se) {
            logger.error(socket);
            se.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
