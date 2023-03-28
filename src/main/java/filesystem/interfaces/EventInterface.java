package filesystem.interfaces;

import java.net.Socket;

public interface EventInterface {
    void handleEvent(Event e, Socket s);
}
