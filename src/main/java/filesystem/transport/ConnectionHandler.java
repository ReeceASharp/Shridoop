package filesystem.transport;

import java.net.Socket;
import java.net.URL;
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

    public synchronized void addConnection(SocketStream ss) {
        connections.add(ss);
    }

    public synchronized void removeConnection(Socket socket) {
        connections.remove(getSocketStream(socket));
    }

    public synchronized SocketStream getSocketStream(Socket socket) {
        Optional<SocketStream> server = connections.stream().filter(socketStream -> socketStream.socket.equals(socket)).findFirst();
        return server.orElse(null);
    }

    public synchronized SocketStream getSocketStream(int index) {
        return connections.get(index);
    }

    public synchronized SocketStream getSocketStream(URL url) {
        Optional<SocketStream> server = connections.stream().filter(socketStream -> socketStream.url.equals(url)).findFirst();
        return server.orElse(null);
    }

}
