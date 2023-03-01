package filesystem.pool;

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
