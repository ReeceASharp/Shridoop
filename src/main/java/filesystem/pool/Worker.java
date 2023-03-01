package filesystem.pool;

import java.util.concurrent.BlockingQueue;

public class Worker implements Runnable {
    BlockingQueue<Task> currentTasks;

    public Worker(BlockingQueue<Task> currentTasks) {
        this.currentTasks = currentTasks;
    }

    @Override
    public void run() {
        Task task = currentTasks.remove();
        task.run();
    }
}
