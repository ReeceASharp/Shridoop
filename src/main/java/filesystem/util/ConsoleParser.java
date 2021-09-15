package filesystem.util;

import filesystem.node.Node;
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

    private final Node node;
    private final Scanner userInput;


    public ConsoleParser(Node node) {
        this.setup(node);

        this.node = node;
        this.userInput = new Scanner(System.in);
    }

    private void setup(Node node) {
        resolveCommands(node);
    }

    private void resolveCommands(Node node) {

        this.commandList.put("commands", userInput -> this.commandList.keySet().toString());
        this.commandList.put("help", userInput -> node.help());
        this.commandList.put("quit", userInput -> null);

        // Get the node specific commands, and their mappings
        this.commandList.putAll(node.getCommandList());
    }

    @Override
    public void run() {
        Thread.currentThread().setName(getClass().getSimpleName());
        System.out.println(node.intro());

        parseInput();

        node.cleanup();
        logger.debug("Exiting ConsoleParser.");
    }

    private void parseInput() {
        while (true) {

            System.out.print("Command: ");
            String input = userInput.nextLine();

            String result;
            //Compare the input command with known commands
            try {
                Command func = commandList.get(input.split(" ")[0].toLowerCase());
                result = func.runCommand(input);
            } catch (NullPointerException npe) {
                result = "Invalid Command.";
            } catch (Exception e) {
                result = "Incorrect Parameters.";
            }

            if (result == null)
                return;

            System.out.println(result);
        }
    }

}
