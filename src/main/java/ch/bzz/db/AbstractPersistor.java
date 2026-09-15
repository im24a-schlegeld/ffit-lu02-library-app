package ch.bzz.db;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.bzz.Config;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public abstract class AbstractPersistor<T> implements AutoCloseable {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final EntityManagerFactory entityManagerFactory;

    protected AbstractPersistor() {
        entityManagerFactory = Persistence.createEntityManagerFactory("localPU", Config.getProperties());
    }

    public void save(T entity) {
        executeTransaction(em -> em.merge(entity));
    }

    public void saveAll(List<T> entities) {
        executeTransaction(em -> entities.forEach(em::merge));
    }

    protected void executeTransaction(Consumer<EntityManager> action) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            try {
                em.getTransaction().begin();
                action.accept(em);
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                log.error("Error during transaction", e);
                throw e;
            }
        }
    }

    @Override
    public void close() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            log.info("EntityManagerFactory closed");
        }
    }
}