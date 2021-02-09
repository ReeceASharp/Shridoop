package fileSystem.transport;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Wrapps the socket streams, and holds onto the connection stream information, so that each TCPSender/Receiver
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
        for (SocketStream ss : connections)
            if (ss.socket.equals(socket)) {
                System.out.println("FOUND SOCKET: " + socket);
                return ss;
            }
        System.out.println("WAS NOT ABLE TO FIND CONNECTION: " + socket);
        return null;
    }


}
