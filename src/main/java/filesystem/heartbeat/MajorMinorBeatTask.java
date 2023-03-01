package filesystem.heartbeat;

import filesystem.protocol.Protocol;

import java.util.TimerTask;

public class MajorMinorBeatTask extends TimerTask {
    private final HeartBeat hb;
    // nth beat, send out a major beat instead of a minor
    private final int beatsBetweenMajor;
    // beat #
    private int currentBeats = 0;

    public MajorMinorBeatTask(HeartBeat hb, int beatsBetweenMajor) {
        this.hb = hb;
        this.beatsBetweenMajor = beatsBetweenMajor;
    }

    @Override
    public void run() {
        //get the current beat type and pass it off to the Controller to handle
        int type = currentBeats == beatsBetweenMajor ? Protocol.HEARTBEAT_MAJOR : Protocol.HEARTBEAT_MINOR;

//        TODO: FIX
//        type = Protocol.HEARTBEAT_MINOR;HEARTBEAT_MINOR

        hb.onHeartBeat(type);

        //update beatCount
        currentBeats++;
        if (type == Protocol.HEARTBEAT_MAJOR)
            currentBeats = 0;
    }
}
