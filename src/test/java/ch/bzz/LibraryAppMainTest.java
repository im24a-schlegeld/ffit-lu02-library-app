package ch.bzz;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class LibraryAppMainTest {

    @Test
    void shouldListHelpCommands() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            LibraryAppMain.executeCommand("help");
        } finally {
            System.setOut(originalOut);
        }

        String text = output.toString();
        assertTrue(text.contains("help"));
        assertTrue(text.contains("quit"));
    }

    @Test
    void shouldRecognizeUnknownCommand() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            boolean shouldContinue = LibraryAppMain.executeCommand("unknown");
            assertTrue(shouldContinue);
        } finally {
            System.setOut(originalOut);
        }

        String text = output.toString();
        assertTrue(text.toLowerCase().contains("not recognized") || text.toLowerCase().contains("unbekannter befehl"));
    }

    @Test
    void shouldExitWhenQuitCommandIsEntered() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));

        try {
            boolean shouldContinue = LibraryAppMain.executeCommand("quit");
            assertFalse(shouldContinue);
        } finally {
            System.setOut(originalOut);
        }

        String text = output.toString();
        assertTrue(text.toLowerCase().contains("quit") || text.toLowerCase().contains("beenden"));
    }
}
