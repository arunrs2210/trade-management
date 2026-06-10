package com.shnoor.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD contract for all DAO classes.
 *
 * @param <T>  Entity type
 * @param <ID> Primary-key type
 */
public interface GenericDAO<T, ID> {

    T       save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean update(T entity);
    boolean delete(ID id);
}
