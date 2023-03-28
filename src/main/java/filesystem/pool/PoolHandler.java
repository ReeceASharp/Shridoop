package filesystem.pool;

import filesystem.interfaces.Event;
import filesystem.interfaces.Task;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles the organization of the thread-pools, which runs
 */
public class PoolHandler {
    final ArrayList<Worker> workers;
    final BlockingQueue<Task> currentTasks;

    public PoolHandler() {
        workers = new ArrayList<>();
        currentTasks = new LinkedBlockingQueue<>();
    }

    public void initializeWorkers(int workerCount) {
        for (int i = 0; i < workerCount; i++) {
            workers.add(new Worker(currentTasks));
        }
    }


    public synchronized void addTask(Task t) {
        currentTasks.add(t);
    }

    public synchronized void addTask(Event e) {

    }


}
