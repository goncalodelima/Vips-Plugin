package com.minecraftsolutions.vip.model.user.repository;

import com.minecraftsolutions.vip.model.user.User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationRepository {

    void setup();

    void insert(User user);

    CompletableFuture<Boolean> update(User user);

    CompletableFuture<Void> update(Collection<User> users);

    Optional<User> findOne(UUID uuid);

    Set<User> findVips();

}
