package fileSystem.util;

import fileSystem.node.controller.Controller;
import fileSystem.protocols.Protocol;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Used by the
 */
public class HeartbeatTimer {
    private Timer beatTimer;

    // time, in seconds, between beats
    private final int heartbeatDelay;
    // on nth beat, send out a major beat instead of a minor
    private final int beatsBetweenMajor;
    // beat #
    private int currentBeats;


    private final Controller controller;

    public HeartbeatTimer(int heartbeatDelay, int beatsBetweenMajor, Controller controller) {
        this.heartbeatDelay = heartbeatDelay;
        this.beatsBetweenMajor = beatsBetweenMajor;
        this.controller = controller;

        currentBeats = 0;
    }

    public void start() {
        //create a new timer, and schedule to start at delay
        beatTimer = new Timer("Heartbeat Timer");
        beatTimer.scheduleAtFixedRate(new beatEvent(), heartbeatDelay * 1000, heartbeatDelay * 1000);
    }

    public void stop() {
        beatTimer.cancel();
    }


    private class beatEvent extends TimerTask {

        @Override
        public void run() {
            //get the current beat type and pass it off to the Controller to handle
            int status = currentBeats == beatsBetweenMajor ? Protocol.HEARTBEAT_MAJOR : Protocol.HEARTBEAT_MINOR;
            controller.sendHeartbeat(status);

            //update beatCount
            currentBeats++;
            if (status == Protocol.HEARTBEAT_MAJOR)
                currentBeats = 0;
        }
    }


}
