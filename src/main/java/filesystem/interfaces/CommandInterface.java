package filesystem.interfaces;

import java.util.Map;

public interface CommandInterface {
    Map<String, Command> getCommandList();
    String help();
    String intro();
    String connectionInfo();
    void exit();
}
