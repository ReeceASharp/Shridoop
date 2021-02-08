package fileSystem.transport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketStream {
    public final Socket socket;
    public final ObjectOutputStream outStream;
    //public final ObjectInputStream inStream;

    public SocketStream(Socket socket) throws IOException {
        this.socket = socket;
        outStream = new ObjectOutputStream(socket.getOutputStream());
        //inStream = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public String toString() {
        return "SocketStream{" +
                "socket=" + socket +
                ", outStream=" + outStream +
                '}';
    }
}
