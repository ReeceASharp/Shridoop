package filesystem.util;

import filesystem.node.ChunkServer;
import filesystem.node.Controller;
import filesystem.node.Node;
import filesystem.protocol.Protocol;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Used by the
 */
public class HeartbeatHandler {
    // time, in seconds, between beats
    private final int heartbeatDelay;
    // on nth beat, send out a major beat instead of a minor
    private final int beatsBetweenMajor;
    private final boolean isActive;
    private final Node node;
    private Timer beatTimer;
    // beat #
    private int currentBeats;

    public HeartbeatHandler(int heartbeatDelay, int beatsBetweenMajor, Node node) {
        this.heartbeatDelay = heartbeatDelay;
        this.beatsBetweenMajor = beatsBetweenMajor;
        this.node = node;

        this.isActive = false;
        currentBeats = 0;
    }

    public void start() {
        if (!isActive) {
            //create a new timer, and schedule to start at delay
            beatTimer = new Timer("Heartbeat Timer");
            beatTimer.scheduleAtFixedRate(new beatEvent(), heartbeatDelay * 1000, heartbeatDelay * 1000);
        }
    }

    public void stop() {
        if (this.isActive)
            beatTimer.cancel();
    }

    private class beatEvent extends TimerTask {

        @Override
        public void run() {
            //get the current beat type and pass it off to the Controller to handle
            int type = currentBeats == beatsBetweenMajor ? Protocol.HEARTBEAT_MAJOR : Protocol.HEARTBEAT_MINOR;

            if (node instanceof Controller) {
                ((Controller) node).onHeartBeat(type);
            }
            if (node instanceof ChunkServer) {
                ((ChunkServer) node).onHeartBeat(type);
            }


            //update beatCount
            currentBeats++;
            if (type == Protocol.HEARTBEAT_MAJOR)
                currentBeats = 0;
        }
    }


}
