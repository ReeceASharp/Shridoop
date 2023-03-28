package filesystem.interfaces;

import java.net.Socket;

public interface ServerInterface {
    boolean newServerConnection(Socket socket);
}
