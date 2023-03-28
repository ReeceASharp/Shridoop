package filesystem.console;

import filesystem.interfaces.CommandInterface;
import filesystem.interfaces.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
Used by an implementation of a Node to give functionality for entering commands
 */
public class ConsoleParser implements Runnable {
    private static final Logger logger = LogManager.getLogger(ConsoleParser.class);

    private final Map<String, Command> commandList = new HashMap<>();
    private final CommandInterface commandInterface;

    private final Scanner userInput;


    public ConsoleParser(CommandInterface commandInterface) {
        this.commandInterface = commandInterface;

        this.setup(commandInterface);

        this.userInput = new Scanner(System.in);
    }

    private void setup(CommandInterface commandInterface) {
        resolveCommands(commandInterface);
    }

    private void resolveCommands(CommandInterface commandInterface) {
        this.commandList.put("commands", userInput -> this.commandList.keySet().toString());
        this.commandList.put("help", userInput -> commandInterface.help());
        this.commandList.put("quit", userInput -> null);
        this.commandList.put("connections", userInput -> commandInterface.connectionInfo());

        // Get the node specific commands, and their mappings
        this.commandList.putAll(commandInterface.getCommandList());
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());
        System.out.println(commandInterface.intro());

        // Read in and Handle input
        parseInput();

        commandInterface.exit();
        logger.debug("Exiting ConsoleParser.");
    }

    private void parseInput() {
        while (true) {
            String input = userInput.nextLine();

            String result;
            try {
                Command func = commandList.get(input.split(" ")[0].toLowerCase());
//                TODO: Put into the node's queue to be handled (this is needed in case a race condition occurs with
//                 services shutting down as a user inputs a command)

                result = func.runCommand(input);
            } catch (NullPointerException npe) {
                result = "Invalid Command.";
            } catch (Exception e) {
                e.printStackTrace();
                result = "Incorrect Parameters.";
            }

            if (result == null)
                return;

            System.out.println(result);
        }
    }

}
