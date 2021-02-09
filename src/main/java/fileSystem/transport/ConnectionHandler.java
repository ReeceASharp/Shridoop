package fileSystem.transport;

import fileSystem.metadata.ServerMetadata;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Wraps the socket streams, and holds onto the connection stream information, so that each TCPSender/Receiver
 * isn't reconstructing the objectstreams and causing corruption exceptions.
 */
public class ConnectionHandler {
    private final ArrayList<SocketStream> connections;


    public ConnectionHandler() {
        this.connections = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ConnectionHandler{" +
                "connections=" + connections +
                '}';
    }

    public void addConnection(SocketStream ss) {
        connections.add(ss);
    }

    public void removeConnection(Socket socket) {
        connections.remove(getSocketStream(socket));
    }

    public SocketStream getSocketStream(Socket socket) {
        Optional<SocketStream> server = connections.stream().filter(socketStream->socketStream.socket.equals(socket)).findFirst();
        return server.orElse(null);
    }


}
