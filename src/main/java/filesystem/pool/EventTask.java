package filesystem.pool;

import filesystem.protocol.Event;

import java.net.Socket;


public class EventTask implements Task {
    Event event;
    Socket socket;
    EventAction actionToRun;

    public EventTask(Event e, Socket s, EventAction c) {
        this.event = e;
        this.socket = s;
        this.actionToRun = c;
    }

    @Override
    public void run() {
        actionToRun.runAction(event, socket);
    }
}
