package br.edu.ifba.saj.fwads.service;

import br.edu.ifba.saj.fwads.model.AbstractEntity;
import br.edu.ifba.saj.fwads.repository.Repository;

import java.util.List;
import java.util.Map;

public class Service<T extends AbstractEntity> {

    protected Repository<T> repository;

    private final Class<T> entityClass;

    public Service(Class<T> entityClass) {
        this.entityClass = entityClass;
        repository = new Repository<>(this.entityClass);
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public List<T> findByMap(Map<String, Object> param) {
        return repository.findByMap(param);
    }

    public T create(T entity) {
        return repository.create(entity);
    }

    public T read(T entity) {
        return repository.read(entity);
    }

    public T update(T entity) {
        return repository.update(entity);
    }

    public void delete(T entity) {
        repository.delete(entity);
    }

    public Long count() {
        return repository.count();
    }

    public void clearCache() {
        repository.clearCache();
    }
}