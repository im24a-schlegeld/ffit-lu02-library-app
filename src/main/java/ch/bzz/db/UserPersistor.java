package ch.bzz.db;

import java.util.Optional;

import ch.bzz.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class UserPersistor extends AbstractPersistor<User> {

    public UserPersistor() {
    }

    public Optional<User> findByEmail(String email) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            User user = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.ofNullable(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

}
