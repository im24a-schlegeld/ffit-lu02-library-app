package ch.bzz;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Config {
    private Config() {
    }

    public static Map<String, String> getProperties() {
        Map<String, String> props = new HashMap<>();

        Properties fileProps = new Properties();
        Path configPath = Path.of("config.properties");
        if (Files.exists(configPath)) {
            try (InputStream in = Files.newInputStream(configPath)) {
                fileProps.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read config.properties", e);
            }
        }

        String url = firstNonBlank(fileProps.getProperty("jakarta.persistence.jdbc.url"),
                System.getProperty("db.url"), "jdbc:h2:mem:librarydb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE");
        String user = firstNonBlank(fileProps.getProperty("jakarta.persistence.jdbc.user"),
                System.getProperty("db.user"), "sa");
        String password = firstNonBlank(fileProps.getProperty("jakarta.persistence.jdbc.password"),
                System.getProperty("db.password"), "");
        String jwtSecret = firstNonBlank(fileProps.getProperty("jwt.secret"),
                System.getProperty("jwt.secret"), "library-app-secret-key-1234567890");

        props.put("jakarta.persistence.jdbc.url", url);
        props.put("jakarta.persistence.jdbc.user", user);
        props.put("jakarta.persistence.jdbc.password", password);
        props.put("jwt.secret", jwtSecret);

        if (url.startsWith("jdbc:h2:")) {
            props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        } else {
            props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        }

        return props;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
