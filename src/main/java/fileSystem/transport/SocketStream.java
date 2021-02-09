package fileSystem.transport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketStream {
    public final Socket socket;
    public final ObjectOutputStream outStream;
    public ObjectInputStream inStream;

    public SocketStream(Socket socket) throws IOException {
        this.socket = socket;
        outStream = new ObjectOutputStream(socket.getOutputStream());
        outStream.flush();
        //inStream = new ObjectInputStream(socket.getInputStream());
        System.out.println("CREATING NEW SOCKETSTREAM: " + socket);
    }

    @Override
    public String toString() {
        return "SocketStream{" +
                "socket=" + socket +
                ", outStream=" + outStream +
                ", inStream=" + inStream +
                '}';
    }

    public void cleanup() {

        try {
            inStream.close();
            outStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
