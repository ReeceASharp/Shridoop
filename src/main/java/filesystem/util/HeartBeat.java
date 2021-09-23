package filesystem.util;

@FunctionalInterface
public interface HeartBeat {
    void onHeartBeat(int type);
}
