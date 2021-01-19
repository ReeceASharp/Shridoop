package fileSystem.node;

import static fileSystem.util.ConsoleConstants.*;

public abstract class Node {

    // Note: Java 8 doesn't allow private methods in an interface, only in Java 9
    // As a result this must be an abstract class
    public String getConsoleText(int type) {
        switch (type) {
            case CONSOLE_INTRO:
                return getIntro();
            case CONSOLE_HELP:
                return getHelp();
            case CONSOLE_COMMANDS:
                return getCommands();
        }
        // TODO: Throw error?
        return "ERROR";
    }

    protected abstract String getHelp();
    protected abstract String getIntro();
    protected abstract String getCommands();

    //when receiving a command from a given TCP thread
    public abstract void onCommand(int type);


}
