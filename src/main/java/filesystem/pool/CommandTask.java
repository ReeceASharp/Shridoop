package filesystem.pool;

import filesystem.interfaces.Command;
import filesystem.interfaces.Task;

public class CommandTask implements Task {
    final String input;
    final Command command;

    public CommandTask(String input, Command command) {
        this.input = input;
        this.command = command;
    }

    @Override
    public void run() {
        command.runCommand(input);
    }
}
