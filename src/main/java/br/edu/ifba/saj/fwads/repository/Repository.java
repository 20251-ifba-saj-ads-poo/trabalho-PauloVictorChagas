package br.edu.ifba.saj.fwads.repository;

import br.edu.ifba.saj.fwads.model.AbstractEntity;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

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
        Path importFile = Paths.get("src/main/resources/import.sql");

        if (!Files.exists(importFile)) {
            return; 
        }

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            String sql = Files.readString(importFile, StandardCharsets.UTF_8);
            for (String command : sql.split(";")) {
                String trimmed = command.trim();
                if (!trimmed.isEmpty()) {
                    em.createNativeQuery(trimmed).executeUpdate();
                }
            }

            em.getTransaction().commit();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler import.sql", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao executar import.sql", e);
        }
    }

    public T create(T entity) {
        return executeInsideTransaction(em -> {
            em.persist(entity);
            em.flush(); // Força o flush imediato
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
            // Forçar uma nova transação para evitar problemas de cache
            em.getTransaction().begin();
            
            StringBuilder jpql = new StringBuilder("SELECT t FROM " + entityClass.getSimpleName() + " t WHERE 1=1");

            params.forEach((k, v) -> jpql.append(" AND t.").append(k).append(" = :").append(k));

            TypedQuery<T> query = em.createQuery(jpql.toString(), entityClass);
            params.forEach(query::setParameter);

            // Desabilitar cache para esta query
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
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            cq.select(cb.count(cq.from(entityClass)));
            return em.createQuery(cq).getSingleResult();
        }
    }

    public void clearCache() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getEntityManagerFactory().getCache().evictAll();
            if (em.isOpen()) {
                em.clear();
            }
        }
    }

    private <R> R executeInsideTransaction(EntityOperation<T, R> operation) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = operation.apply(em);
            tx.commit();
            System.out.println("Transação commitada com sucesso");
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
                System.out.println("Transação revertida devido a erro: " + e.getMessage());
            }
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