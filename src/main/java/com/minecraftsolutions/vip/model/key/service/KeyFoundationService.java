package com.minecraftsolutions.vip.model.key.service;

import com.minecraftsolutions.vip.model.key.Key;

import java.util.List;
import java.util.Optional;

public interface KeyFoundationService {

    void put(Key key);

    Optional<Key> get(String name);

    void remove(Key key);

    List<Key> getAll();

}
