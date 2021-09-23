package filesystem.util.taskscheduler;

import filesystem.util.HeartBeat;
import filesystem.protocol.Protocol;

import java.util.TimerTask;

public class HeartBeatTask extends TimerTask {
    private final HeartBeat heartBeatObject;

    public HeartBeatTask(HeartBeat heartBeatObject) {
        this.heartBeatObject = heartBeatObject;
    }

    @Override
    public void run() {
        heartBeatObject.onHeartBeat(Protocol.HEARTBEAT_IGNORE_TYPE);
    }
}
