package filesystem.pool;

import filesystem.interfaces.Event;
import filesystem.interfaces.EventInterface;
import filesystem.interfaces.Task;

import java.net.Socket;


public class EventTask implements Task {
    Event event;
    Socket socket;
    EventInterface actionToRun;

    public EventTask(Event e, Socket s, EventInterface c) {
        this.event = e;
        this.socket = s;
        this.actionToRun = c;
    }

    @Override
    public void run() {
        actionToRun.handleEvent(event, socket);
    }
}
