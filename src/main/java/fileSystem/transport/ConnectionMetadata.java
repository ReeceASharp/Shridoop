package fileSystem.transport;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Wraps the socket streams, and holds onto the connection stream information, so that each TCPSender/Receiver
 * isn't reconstructing the objectstreams and causing corruption exceptions.
 */
public class ConnectionMetadata {
    private final ArrayList<SocketStream> connections;


    public ConnectionMetadata() {
        this.connections = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ConnectionHandler{" +
                "connections=" + connections +
                '}';
    }

    public synchronized void addConnection(SocketStream ss) {
        connections.add(ss);
    }

    public synchronized void removeConnection(Socket socket) {
        connections.remove(getSocketStream(socket));
    }

    public synchronized SocketStream getSocketStream(Socket socket) {
        Optional<SocketStream> server = connections.stream().filter(socketStream->socketStream.socket.equals(socket)).findFirst();
        return server.orElse(null);
    }

    public synchronized SocketStream getSocketStream(int index) {
        return connections.get(index);
    }

    public synchronized SocketStream getSocketStream(String hostPort) {
        Optional<SocketStream> server = connections.stream().filter(socketStream->socketStream.hostPort.equals(hostPort)).findFirst();
        return server.orElse(null);
    }

    public synchronized SocketStream getSocketStream(String host, int port) {
        return getSocketStream(String.format("%s:%d", host, port));
    }


}
