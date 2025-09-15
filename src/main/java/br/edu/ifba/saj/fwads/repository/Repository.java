package br.edu.ifba.saj.fwads.repository;

import br.edu.ifba.saj.fwads.model.AbstractEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Repository<T extends AbstractEntity> {

    private static final EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("jpa");
            runImport();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError("Erro ao inicializar EntityManagerFactory: " + ex);
        }
    }

    private final Class<T> entityClass;

    public Repository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    private static void runImport() {
        try (InputStream is = Repository.class.getResourceAsStream("/import.sql")) {
            if (is == null) return;
            String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            try (EntityManager em = emf.createEntityManager()) {
                em.getTransaction().begin();
                for (String command : sql.split(";")) {
                    String trimmed = command.trim();
                    if (!trimmed.isEmpty()) {
                        em.createNativeQuery(trimmed).executeUpdate();
                    }
                }
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao executar import.sql", e);
        }
    }

    public T create(T entity) {
        return executeInsideTransaction(em -> {
            em.persist(entity);
            em.flush();
            return entity;
        });
    }

    public T read(Object id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(entityClass, id);
        }
    }

    public List<T> findAll() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT t FROM " + entityClass.getSimpleName() + " t";
            return em.createQuery(jpql, entityClass).getResultList();
        }
    }

    public List<T> findByMap(Map<String, Object> params) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            
            StringBuilder jpql = new StringBuilder("SELECT t FROM " + entityClass.getSimpleName() + " t WHERE 1=1");

            params.forEach((k, v) -> jpql.append(" AND t.").append(k).append(" = :").append(k));

            TypedQuery<T> query = em.createQuery(jpql.toString(), entityClass);
            params.forEach(query::setParameter);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            query.setHint("javax.persistence.cache.retrieveMode", "BYPASS");
            
            List<T> result = query.getResultList();
            em.getTransaction().commit();
            
            return result;
        }
    }

    public T update(T entity) {
        return executeInsideTransaction(em -> em.merge(entity));
    }

    public void delete(T entity) {
        executeInsideTransaction(em -> {
            T managed = em.contains(entity) ? entity : em.merge(entity);
            em.remove(managed);
            return null;
        });
    }

    public Long count() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT COUNT(t) FROM " + entityClass.getSimpleName() + " t";
            return em.createQuery(jpql, Long.class).getSingleResult();
        }
    }

    public void clearCache() {
        try {
            emf.getCache().evictAll();
        } catch (Exception e) {
        }
    }

    private <R> R executeInsideTransaction(EntityOperation<T, R> operation) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = operation.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @FunctionalInterface
    private interface EntityOperation<T, R> {
        R apply(EntityManager em);
    }
}