package filesystem.transport;

import filesystem.protocol.Event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketStream {
    public final Socket socket;
    public final InetSocketAddress address;
    public ObjectOutputStream outStream;
    public ObjectInputStream inStream;

    public SocketStream(Socket socket) throws IOException {
        this.socket = socket;
        this.address = new InetSocketAddress(socket.getInetAddress(), socket.getPort());

        // DEAR GOD OUT BEFORE IN
        this.outStream = new ObjectOutputStream(socket.getOutputStream());
        this.inStream = new ObjectInputStream(socket.getInputStream());
        this.outStream.flush();

    }

    @Override
    public String toString() {
        return "SocketStream{" +
                       "socket=" + socket +
                       ", outStream=" + outStream +
                       ", inStream=" + inStream +
                       '}';
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
