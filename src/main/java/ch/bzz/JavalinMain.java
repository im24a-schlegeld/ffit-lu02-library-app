package ch.bzz;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.bzz.db.BookPersistor;
import ch.bzz.db.UserPersistor;
import ch.bzz.model.Book;
import ch.bzz.model.User;
import ch.bzz.security.JwtHandler;
import ch.bzz.security.PasswordHandler;
import io.jsonwebtoken.Claims;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class JavalinMain {
    private static final int PORT = 7070;

    public static void main(String[] args) {
        ensureSeedUser();
        Javalin app = Javalin.create();
        app.get("/books", JavalinMain::handleBooks);
        app.post("/auth/login", JavalinMain::handleLogin);
        app.put("/auth/change-password", JavalinMain::handleChangePassword);
        app.start(PORT);
    }

    private static void ensureSeedUser() {
        try (UserPersistor persistor = new UserPersistor()) {
            if (persistor.findByEmail("max.mustermann@example.com").isPresent()) {
                return;
            }

            User user = new User();
            user.setFirstname("Max");
            user.setLastname("Mustermann");
            user.setDateOfBirth(java.time.LocalDate.of(1990, 5, 21));
            user.setEmail("max.mustermann@example.com");
            byte[] salt = PasswordHandler.generateSalt();
            user.setPasswordHash(PasswordHandler.hashPassword("geheim123", salt));
            user.setPasswordSalt(salt);
            persistor.save(user);
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize default test user", e);
        }
    }

    private static void handleBooks(Context ctx) {
        int limit = 10;
        try {
            String limitParam = ctx.queryParam("limit");
            if (limitParam != null) {
                limit = Integer.parseInt(limitParam);
            }
        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.result("Invalid limit");
            return;
        }

        try (BookPersistor persistor = new BookPersistor()) {
            List<Book> books = persistor.getAll(limit);
            ctx.json(books);
        }
    }

    private static void handleLogin(Context ctx) {
        Map<String, Object> body;
        try {
            body = ctx.bodyAsClass(Map.class);
        } catch (Exception e) {
            ctx.status(400);
            ctx.result("Invalid JSON");
            return;
        }

        String email = body.getOrDefault("email", "").toString().trim();
        String password = body.getOrDefault("password", "").toString();

        if (email.isBlank() || password.isBlank()) {
            ctx.status(400);
            ctx.result("Email and password required");
            return;
        }

        try (UserPersistor persistor = new UserPersistor()) {
            Optional<User> userOpt = persistor.findByEmail(email);
            if (userOpt.isEmpty()) {
                ctx.status(401);
                ctx.json(Map.of("error", "Invalid email or password"));
                return;
            }

            User user = userOpt.get();
            boolean valid = PasswordHandler.verifyPassword(password, user.getPasswordHash(), user.getPasswordSalt());
            if (!valid) {
                ctx.status(401);
                ctx.json(Map.of("error", "Invalid email or password"));
                return;
            }

            String token = JwtHandler.createJwt(email, user.getId());
            ctx.json(Map.of("token", token));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(Map.of("error", "Internal server error"));
        }
    }

    private static void handleChangePassword(Context ctx) {
        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.status(401);
            ctx.json(Map.of("error", "Unauthorized"));
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        Claims claims;
        try {
            claims = JwtHandler.parseClaims(token);
        } catch (Exception e) {
            ctx.status(401);
            ctx.json(Map.of("error", "Invalid token"));
            return;
        }

        Map<String, Object> body;
        try {
            body = ctx.bodyAsClass(Map.class);
        } catch (Exception e) {
            ctx.status(400);
            ctx.result("Invalid JSON");
            return;
        }

        String oldPassword = body.getOrDefault("oldPassword", "").toString();
        String newPassword = body.getOrDefault("newPassword", "").toString();
        if (oldPassword.isBlank() || newPassword.isBlank()) {
            ctx.status(400);
            ctx.json(Map.of("error", "oldPassword and newPassword are required"));
            return;
        }

        Integer userId = claims.get("userId", Integer.class);
        if (userId == null) {
            ctx.status(401);
            ctx.json(Map.of("error", "Invalid token"));
            return;
        }

        try (UserPersistor persistor = new UserPersistor()) {
            Optional<User> userOpt = persistor.findById(userId);
            if (userOpt.isEmpty()) {
                ctx.status(404);
                ctx.json(Map.of("error", "User not found"));
                return;
            }

            User user = userOpt.get();
            boolean valid = PasswordHandler.verifyPassword(oldPassword, user.getPasswordHash(), user.getPasswordSalt());
            if (!valid) {
                ctx.status(401);
                ctx.json(Map.of("error", "Invalid old password"));
                return;
            }

            byte[] newSalt = PasswordHandler.generateSalt();
            byte[] newHash = PasswordHandler.hashPassword(newPassword, newSalt);
            user.setPasswordSalt(newSalt);
            user.setPasswordHash(newHash);
            persistor.save(user);
            ctx.json(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(Map.of("error", "Internal server error"));
        }
    }
}
