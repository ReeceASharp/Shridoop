package filesystem.transport;

import filesystem.protocol.Event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class SocketStream {
    public final Socket socket;
    public final URL url;
    public ObjectOutputStream outStream;
    public ObjectInputStream inStream;

    public SocketStream(Socket socket) throws MalformedURLException {
        this.socket = socket;
        this.url = new URL(String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort()));
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

    public synchronized void setup() throws IOException {
        inStream = new ObjectInputStream(socket.getInputStream());
        outStream = new ObjectOutputStream(socket.getOutputStream());
        outStream.flush();
    }

}
