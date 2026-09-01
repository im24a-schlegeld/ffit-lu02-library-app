package ch.bzz;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class LibraryAppMain {

    private static final Map<String, Runnable> COMMANDS = new LinkedHashMap<>();

    static {
        register("help", LibraryAppMain::printHelp);
        register("quit", LibraryAppMain::quitApp);
    }

    public static void main(String[] args) {
        System.out.println("Library App started.");
        printHelp();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Command> ");
                String input = scanner.nextLine();

                if (!executeCommand(input)) {
                    break;
                }
            }
        }
    }

    public static boolean executeCommand(String input) {
        if (input == null) {
            System.out.println("Command not recognized. Type 'help' to list all commands.");
            return true;
        }

        String command = input.trim().toLowerCase(Locale.ROOT);

        if (command.isEmpty()) {
            System.out.println("Command not recognized. Type 'help' to list all commands.");
            return true;
        }

        Runnable action = COMMANDS.get(command);
        if (action != null) {
            action.run();
            return !"quit".equals(command);
        }

        System.out.println("Command '" + input + "' is not recognized. Type 'help' to list all commands.");
        return true;
    }

    private static void register(String commandName, Runnable action) {
        COMMANDS.put(commandName.toLowerCase(Locale.ROOT), action);
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        for (String command : COMMANDS.keySet()) {
            System.out.println(" - " + command);
        }
    }

    private static void quitApp() {
        System.out.println("Quit command received. Application closed.");
    }
}
