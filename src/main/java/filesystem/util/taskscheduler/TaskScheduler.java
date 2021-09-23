package filesystem.util.taskscheduler;

import filesystem.util.Pair;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Used by the
 */
public class TaskScheduler {
    private final Timer scheduleTimer;
    private final ArrayList<Pair<String, TimerTask>> currentTasks;

    public TaskScheduler(String name) {
        scheduleTimer = new Timer(String.format("%s:%s", name, this.getClass().getName()));
        currentTasks = new ArrayList<>();
    }

    public void scheduleAndStart(TimerTask task, String name, long delay, long period) {
        scheduleTimer.scheduleAtFixedRate(task, delay * 1000, period * 1000);
        currentTasks.add(new Pair<>(name, task));
    }

    public ArrayList<Pair<String, TimerTask>> getCurrentTasks() {
        return currentTasks;
    }

}
