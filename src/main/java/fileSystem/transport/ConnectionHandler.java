package fileSystem.transport;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Wrapps the socket streams, and holds onto the connection stream information, so that each TCPSender/Receiver
 * isn't reconstructing the objectstreams and causing corruption exceptions.
 */
public class ConnectionHandler {
    private final ArrayList<SocketStream> connections;


    @Override
    public String toString() {
        return "ConnectionHandler{" +
                "connections=" + connections +
                '}';
    }

    public ConnectionHandler() {
        this.connections = new ArrayList<>();
    }

    public SocketStream addConnection(Socket socket) throws IOException {
        SocketStream stream = new SocketStream(socket);
        connections.add(stream);
        return stream;
    }

    public void removeConnection(Socket socket) {
        connections.remove(getSocketStream(socket));
    }

    public SocketStream getSocketStream(Socket socket) {
        for (SocketStream ss : connections)
            if (ss.socket.equals(socket))
                return ss;
        return null;
    }



}
