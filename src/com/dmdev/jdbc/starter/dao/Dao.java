package com.dmdev.jdbc.starter.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<K, E> {
    List<E> findAll();
    Optional<E> findById(K key);
    boolean update(E entity);
    E save(E entity);
    boolean delete(K key);
}
