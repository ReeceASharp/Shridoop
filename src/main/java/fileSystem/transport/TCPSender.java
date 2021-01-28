package fileSystem.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Organizes how marshalled data is sent
 */
public class TCPSender implements Runnable {
    private final Socket socket;
    private final byte[] dataToSend;

    public TCPSender(Socket socket, byte[] data) {
        this.socket = socket;
        this.dataToSend = data;
    }

    @Override
    public void run() {
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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
