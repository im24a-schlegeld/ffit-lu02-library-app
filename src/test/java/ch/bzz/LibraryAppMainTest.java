package ch.bzz;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.bzz.model.Book;
import ch.bzz.model.User;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

class LibraryAppMainTest {
    private static EntityManagerFactory emf;

    @BeforeAll
    static void insertTestData() {
        emf = Persistence.createEntityManagerFactory("localPU", Config.getProperties());
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(new Book(1, "978-0134685991", "Effective Java", "Joshua Bloch", 2018));
            em.merge(new Book(2, "978-0596009205", "Head First Java", "Kathy Sierra, Bert Bates", 2005));
            em.getTransaction().commit();
        }
    }

    @AfterAll
    static void tearDown() {
        if (emf != null) {
            emf.close();
        }
    }

    @Test
    void shouldListHelpCommands() {
        var output = captureCommand("help");
        assertTrue(output.contains("help"));
        assertTrue(output.contains("quit"));
    }

    @Test
    void shouldRecognizeUnknownCommand() {
        var output = captureCommand("unknown");
        assertTrue(output.toLowerCase().contains("not recognized"));
        assertTrue(LibraryAppMain.executeCommand("unknown"));
    }

    @Test
    void shouldExitWhenQuitCommandIsEntered() {
        var output = captureCommand("quit");
        assertFalse(LibraryAppMain.executeCommand("quit"));
        assertTrue(output.toLowerCase().contains("quit") || output.toLowerCase().contains("bye"));
    }

    @Test
    void testQuitEndsProgramWithoutError() {
        prepareStreams("quit\n");
        assertDoesNotThrow(() -> LibraryAppMain.main(new String[]{}));
    }

    @Test
    void testInvalidCommandContainsInput() {
        var out = prepareStreams("foobar\nquit\n");
        LibraryAppMain.main(new String[]{});
        assertTrue(out.toString().contains("foobar"));
    }

    @Test
    void testHelpCommandContainsAllCommands() {
        var out = prepareStreams("help\nquit\n");
        LibraryAppMain.main(new String[]{});
        String output = out.toString();
        assertTrue(output.contains("help"));
        assertTrue(output.contains("quit"));
        assertTrue(output.contains("listBooks"));
        assertTrue(output.contains("importBooks"));
    }

    @Test
    void testListBooksPrintsExampleBooks() {
        var out = prepareStreams("listBooks\nquit\n");
        LibraryAppMain.main(new String[]{});
        assertTrue(out.toString().contains("Effective Java"));
        assertTrue(out.toString().contains("Head First Java"));
    }

    @Test
    void testListBooksWithLimitOnePrintsOnlyOneBook() {
        var out = prepareStreams("listBooks 1\nquit\n");
        LibraryAppMain.main(new String[]{});
        assertTrue(out.toString().contains("Effective Java"));
        assertFalse(out.toString().contains("Head First Java"));
    }

    @Test
    void testListBooksWithInvalidNumberDoesNotThrow() {
        var out = prepareStreams("listBooks SEVEN\nquit\n");
        assertDoesNotThrow(() -> LibraryAppMain.main(new String[]{}));
        assertFalse(out.toString().isEmpty());
    }

    @Test
    void testImportBooksImportsFromTsv() throws URISyntaxException {
        var resourceUrl = getClass().getClassLoader().getResource("test_books_import.tsv");
        assertNotNull(resourceUrl);
        Path filePath = Paths.get(resourceUrl.toURI());
        var out = prepareStreams("importBooks " + filePath + "\nlistBooks 4\nquit\n");
        LibraryAppMain.main(new String[]{});
        assertTrue(out.toString().contains("Domain-Driven Design"));
        assertTrue(out.toString().contains("Refactoring"));
        assertFalse(out.toString().contains("Clean Architecture"));
    }

    @Test
    void testImportBooksWithInvalidFileDoesNotThrow() {
        assertNull(getClass().getClassLoader().getResource("NONEXISTING.tsv"));
        var out = prepareStreams("importBooks NONEXISTING.tsv\nquit\n");
        assertDoesNotThrow(() -> LibraryAppMain.main(new String[]{}));
        assertFalse(out.toString().isEmpty());
    }

    @Test
    void testCreateUserCommand() {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User u WHERE u.email = 'max.mustermann@example.com'").executeUpdate();
            em.getTransaction().commit();
        }

        prepareStreams("createUser Max Mustermann 1990-05-21 max.mustermann@example.com geheim123\nquit\n");
        LibraryAppMain.main(new String[]{});
        try (var em = emf.createEntityManager()) {
            User user = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", "max.mustermann@example.com")
                    .getResultStream().findFirst().orElse(null);
            assertNotNull(user);
            assertEquals("Max", user.getFirstname());
            assertEquals("Mustermann", user.getLastname());
            assertEquals("1990-05-21", user.getDateOfBirth().toString());
            assertNotNull(user.getPasswordHash());
            assertNotNull(user.getPasswordSalt());
        }
    }

    private String captureCommand(String command) {
        PrintStream originalOut = System.out;
        var output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        try {
            LibraryAppMain.executeCommand(command);
            return output.toString();
        } finally {
            System.setOut(originalOut);
        }
    }

    private ByteArrayOutputStream prepareStreams(String input) {
        InputStream in = new ByteArrayInputStream(input.getBytes());
        var out = new ByteArrayOutputStream();
        System.setIn(in);
        System.setOut(new PrintStream(out));
        return out;
    }
}