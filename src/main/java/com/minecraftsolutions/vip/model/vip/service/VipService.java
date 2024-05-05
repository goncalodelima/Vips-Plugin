package com.minecraftsolutions.vip.model.vip.service;

import com.minecraftsolutions.vip.model.vip.Vip;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class VipService implements VipFoundationService {

    private final Map<String, Vip> cache;

    public VipService() {
        this.cache = new HashMap<>();
    }

    @Override
    public void put(Vip vip) {
        cache.put(vip.getIdentifier().toUpperCase(), vip);
    }

    @Override
    @Nullable
    public Vip get(String identifier) {
        return cache.get(identifier.toUpperCase());
    }

    @Override
    public List<Vip> getAll() {
        return cache
                .keySet()
                .stream()
                .map(this::get)
                .collect(Collectors.toList());
    }

}
