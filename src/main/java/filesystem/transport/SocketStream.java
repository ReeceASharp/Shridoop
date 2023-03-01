package filesystem.transport;

import filesystem.protocol.Event;
import filesystem.util.HostPortAddress;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketStream {
    public final Socket socket;
    public final HostPortAddress address;
    public ObjectOutputStream outStream;
    public ObjectInputStream inStream;
//    TODO: Temp naming schema to dictate where the connections are being created
//    Or do some better logging


    public SocketStream(Socket socket) throws IOException {
        this.socket = socket;
        this.address = new HostPortAddress(socket.getLocalAddress().getCanonicalHostName(), socket.getPort());
//        System.out.println("\nNEW HOSTNAME: "+ );
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
