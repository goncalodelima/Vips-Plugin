package com.minecraftsolutions.vip.model.key.repository;

import com.minecraftsolutions.vip.model.key.Key;

import java.util.List;

public interface KeyFoundationRepository {

    void setup();

    void insert(Key key);

    void remove(Key key);

    Key findOne(String name);

    List<Key> findAll();

}
