package com.minecraftsolutions.vip.model.key.repository;

import com.minecraftsolutions.vip.model.key.Key;

import java.util.Set;

public interface KeyFoundationRepository {

    void setup();

    void insert(Key key);

    void remove(Key key);

    Key findOne(String name);

    Set<Key> findAll();

}
