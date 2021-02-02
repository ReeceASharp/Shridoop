package fileSystem.util;

import fileSystem.node.Heartbeat;
import fileSystem.node.Node;
import fileSystem.node.controller.Controller;
import fileSystem.node.server.ChunkServer;
import fileSystem.protocols.Protocol;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Used by the
 */
public class HeartbeatHandler {
    private Timer beatTimer;

    // time, in seconds, between beats
    private final int heartbeatDelay;
    // on nth beat, send out a major beat instead of a minor
    private final int beatsBetweenMajor;
    // beat #
    private int currentBeats;
    private final boolean isActive;


    private final Node node;

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
