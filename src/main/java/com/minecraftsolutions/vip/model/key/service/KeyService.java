package com.minecraftsolutions.vip.model.key.service;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.key.Key;
import com.minecraftsolutions.vip.model.key.repository.KeyFoundationRepository;
import com.minecraftsolutions.vip.model.key.repository.KeyRepository;

import java.util.*;
import java.util.stream.Collectors;

public class KeyService implements KeyFoundationService {

    private final Map<String, Key> cache;
    private final KeyFoundationRepository keyRepository;

    public KeyService(VipPlugin plugin, Database database) {
        this.cache = new HashMap<>();
        this.keyRepository = new KeyRepository(plugin, database);
    }

    @Override
    public void put(Key key) {
        cache.put(key.getName(), key);
        keyRepository.insert(key);
    }

    @Override
    public Optional<Key> get(String name) {

        Key key = cache.get(name);

        if (key != null)
            return Optional.of(key);

        key = keyRepository.findOne(name);

        if (key != null)
            cache.put(key.getName(), key);

        return Optional.ofNullable(key);
    }

    @Override
    public void remove(Key key) {
        cache.remove(key.getName());
        keyRepository.remove(key);
    }

    @Override
    public List<Key> getAll() {
        return keyRepository.findAll();
    }

}
