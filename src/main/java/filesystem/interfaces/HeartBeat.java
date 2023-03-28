package filesystem.interfaces;

@FunctionalInterface
public interface HeartBeat {
    void onHeartBeat(int type);
}
