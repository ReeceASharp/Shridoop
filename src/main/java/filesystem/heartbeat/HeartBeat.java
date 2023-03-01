package filesystem.heartbeat;

@FunctionalInterface
public interface HeartBeat {
    void onHeartBeat(int type);
}
