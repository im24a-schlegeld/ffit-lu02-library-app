package ch.bzz.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ch.bzz.model.Book;
import jakarta.persistence.EntityManager;

public class BookPersistor extends AbstractPersistor<Book> {

    public BookPersistor() {
        ensureSeedData();
    }

    private void ensureSeedData() {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            Long count = em.createQuery("SELECT COUNT(b) FROM Book b", Long.class).getSingleResult();
            if (count > 0) {
                return;
            }

            Path dataFile = Path.of("data", "books.tsv");
            if (!Files.exists(dataFile)) {
                return;
            }

            List<Book> seedBooks = new ArrayList<>();
            try {
                List<String> lines = Files.readAllLines(dataFile);
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
                        seedBooks.add(new Book(
                                Integer.parseInt(values[0].trim()),
                                values[1].trim(),
                                values[2].trim(),
                                values[3].trim(),
                                Integer.parseInt(values[4].trim())));
                    } catch (NumberFormatException ignored) {
                        // ignore invalid rows
                    }
                }
            } catch (IOException ignored) {
                // ignore if file cannot be read
            }

            if (!seedBooks.isEmpty()) {
                em.getTransaction().begin();
                for (Book book : seedBooks) {
                    em.merge(book);
                }
                em.getTransaction().commit();
            }
        }
    }

    public List<Book> getAll(int limit) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            var query = em.createQuery("SELECT b FROM Book b ORDER BY b.id ASC", Book.class);
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            return query.getResultList();
        }
    }

}
