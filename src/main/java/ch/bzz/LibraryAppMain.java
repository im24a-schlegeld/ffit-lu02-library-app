package ch.bzz;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import ch.bzz.db.BookPersistor;
import ch.bzz.db.UserPersistor;
import ch.bzz.model.Book;
import ch.bzz.model.User;
import ch.bzz.security.PasswordHandler;

public class LibraryAppMain {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Library App ready. Type 'help' for available commands.");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                if (input == null) {
                    break;
                }

                String trimmed = input.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                String[] parts = trimmed.split("\\s+");
                String command = parts[0].toLowerCase(Locale.ROOT);

                switch (command) {
                    case "quit":
                        System.out.println("Bye!");
                        return;
                    case "help":
                        printHelp();
                        break;
                    case "listbooks":
                        handleListBooks(parts);
                        break;
                    case "importbooks":
                        handleImportBooks(parts);
                        break;
                    case "createuser":
                        handleCreateUser(parts);
                        break;
                    default:
                        System.out.println("Unknown command: '" + trimmed + "'");
                        break;
                }
            }
        }
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("- help");
        System.out.println("- quit");
        System.out.println("- listBooks [limit]");
        System.out.println("- importBooks <file>");
        System.out.println("- createUser <firstname> <lastname> <yyyy-mm-dd> <email> <password>");
    }

    private static void handleListBooks(String[] parts) {
        int limit = 0;
        if (parts.length > 1) {
            try {
                limit = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid limit: '" + parts[1] + "'");
                return;
            }
        }

        try (BookPersistor persist = new BookPersistor()) {
            List<Book> books = persist.getAll(limit);
            if (books.isEmpty()) {
                System.out.println("No books found.");
                return;
            }

            for (Book book : books) {
                System.out.println(book.getId() + ": " + book.getTitle() + " by " + book.getAuthors() + " (" + book.getPublicationYear() + ")");
            }
        }
    }

    private static void handleImportBooks(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: importBooks <file>");
            return;
        }

        Path path = Path.of(parts[1]);
        if (!Files.exists(path)) {
            System.out.println("File not found: " + path);
            return;
        }

        try {
            List<Book> books = new ArrayList<>();
            List<String> lines = Files.readAllLines(path);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] values = line.split("\\t");
                if (values.length < 5) {
                    continue;
                }

                try {
                    int id = Integer.parseInt(values[0].trim());
                    String isbn = values[1].trim();
                    String title = values[2].trim();
                    String authors = values[3].trim();
                    int year = Integer.parseInt(values[4].trim());
                    books.add(new Book(id, isbn, title, authors, year));
                } catch (NumberFormatException ignored) {
                    // ignore malformed rows
                }
            }

            try (BookPersistor persist = new BookPersistor()) {
                persist.saveAll(books);
                System.out.println("Imported " + books.size() + " books from " + path);
            }
        } catch (IOException e) {
            System.out.println("Could not read file: " + path);
        }
    }

    private static void handleCreateUser(String[] parts) {
        if (parts.length < 6) {
            System.out.println("Usage: createUser <firstname> <lastname> <yyyy-mm-dd> <email> <password>");
            return;
        }

        try {
            User user = new User();
            user.setFirstname(parts[1]);
            user.setLastname(parts[2]);
            user.setDateOfBirth(LocalDate.parse(parts[3]));
            user.setEmail(parts[4]);

            byte[] salt = PasswordHandler.generateSalt();
            byte[] hash = PasswordHandler.hashPassword(parts[5], salt);
            user.setPasswordHash(hash);
            user.setPasswordSalt(salt);

            try (UserPersistor persist = new UserPersistor()) {
                persist.save(user);
            }

            System.out.println("User created: " + user.getEmail());
        } catch (Exception e) {
            System.out.println("Could not create user: " + e.getMessage());
        }
    }
}
