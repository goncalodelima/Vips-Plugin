package com.minecraftsolutions.vip.model.user.service;

import com.minecraftsolutions.vip.model.user.User;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationService {

    Set<User> getPendingUpdates();

    void put(User user);

    void putData(User user);

    Optional<User> get(String name);

    Optional<User> getData(String name);

    void update(User user);

    CompletableFuture<Void> update(Collection<User> users);

    void remove(String nickname);

    Collection<User> getVips();

}
