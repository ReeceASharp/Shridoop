package filesystem.transport;

import filesystem.util.HostPortAddress;
import filesystem.util.NodeUtils;

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
        return NodeUtils.GenericListFormatter.getFormattedOutput(connections, "|", true);
    }

    public synchronized int size() {
        return connections.size();
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

    public synchronized SocketStream getSocketStream(HostPortAddress address) {
        Optional<SocketStream> server = connections.stream().filter(socketStream -> socketStream.address.equals(address)).findFirst();
        return server.orElse(null);
    }

    public synchronized SocketStream fetchKnownConnection(String nodeID) {

        return null;
    }

    public synchronized SocketStream fetchKnownConnection(Socket incomingConnection) {
        return null;
    }

}
