package com.minecraftsolutions.vip.model.user.service;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.user.repository.UserFoundationRepository;
import com.minecraftsolutions.vip.model.user.repository.UserRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UserService implements UserFoundationService {

    private final UserFoundationRepository userRepository;
    private final Map<UUID, User> cache;
    private final Set<User> pendingUpdates;

    public UserService(VipPlugin plugin, Database database) {

        this.userRepository = new UserRepository(plugin.getVipService(), database, plugin.getAsyncExecutor(), plugin.getLogger());
        this.cache = new HashMap<>();
        this.pendingUpdates = new HashSet<>();

        for (User user : userRepository.findVips()) {
            put(user);
        }

    }

    @Override
    public Set<User> getPendingUpdates() {
        return pendingUpdates;
    }

    @Override
    public void put(User user) {
        cache.put(user.getUuid(), user);
    }

    @Override
    public void putData(User user) {
        userRepository.insert(user);
    }

    @Override
    public Optional<User> get(UUID uuid) {
        User user = cache.get(uuid);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> getData(UUID uuid) {
        return userRepository.findOne(uuid);
    }

    @Override
    public void update(User user) {
        pendingUpdates.add(user);
    }

    @Override
    public CompletableFuture<Void> update(Collection<User> users) {
        return userRepository.update(users);
    }

    @Override
    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public Collection<User> getVips() {
        return cache.values();
    }

}
