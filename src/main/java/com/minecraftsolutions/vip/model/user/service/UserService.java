package com.minecraftsolutions.vip.model.user.service;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.user.repository.UserFoundationRepository;
import com.minecraftsolutions.vip.model.user.repository.UserRepository;

import java.util.*;

public class UserService implements UserFoundationService {

    private final UserFoundationRepository userRepository;
    private final Map<String, User> cache;

    public UserService(VipPlugin plugin, Database database) {
        this.userRepository = new UserRepository(plugin, database);
        this.cache = new HashMap<>();
    }

    @Override
    public void put(User user) {
        cache.put(user.getName(), user);
        userRepository.insert(user);
    }

    @Override
    public Optional<User> get(String name) {

        User user = cache.get(name);

        if (user != null)
            return Optional.of(user);

        user = userRepository.findOne(name);

        if (user != null)
            this.cache.put(user.getName(), user);

        return Optional.ofNullable(user);
    }

    @Override
    public void update(User user) {
        userRepository.update(user);
    }

    @Override
    public void remove(User user) {
        cache.remove(user.getName());
    }

    @Override
    public List<User> getVips() {

        List<User> vips = new ArrayList<>();
        List<User> databaseVips = userRepository.findVips();

        for (User user : cache.values()) {
            if (user.getEnabledVip() != null) {
                vips.add(user);
            }
        }

        if (databaseVips != null) {
            for (User user : databaseVips) {
                if (!cache.containsKey(user.getName())) {
                    vips.add(user);
                }
            }
        }

        return vips;
    }

}
