package fileSystem.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Used by the
 */
public class HeartbeatTimer {

    // time, in seconds, between beats
    private int heartbeatDelay;
    // on nth beat, send out a major beat instead of a minor
    private int beatsUntilMajor;

    HeartbeatTimer(int heartbeatDelay, int beatsUntilMajor) {
        this.heartbeatDelay = heartbeatDelay;
    }

    public void start() {
        //create a new timer, and schedule to start at delay
        Timer beatTimer = new Timer("Heartbeat Timer");
        beatTimer.scheduleAtFixedRate(new beatEvent(),heartbeatDelay * 1000, heartbeatDelay * 1000);
    }

    private class beatEvent extends TimerTask {

        @Override
        public void run() {

        }
    }


}
