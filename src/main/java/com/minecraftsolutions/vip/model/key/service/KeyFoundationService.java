package com.minecraftsolutions.vip.model.key.service;

import com.minecraftsolutions.vip.model.key.Key;

import java.util.Optional;
import java.util.Set;

public interface KeyFoundationService {

    void put(Key key);

    Optional<Key> get(String name);

    void remove(Key key);

    Set<Key> getAll();

}
