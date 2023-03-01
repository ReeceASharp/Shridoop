package filesystem.pool;

import filesystem.protocol.Event;

import java.net.Socket;

public interface EventAction {
    void runAction(Event e, Socket s);
}
