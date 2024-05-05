package com.minecraftsolutions.vip.model.user.service;

import com.minecraftsolutions.vip.model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserFoundationService {

    void put(User user);

    Optional<User> get(String name);

    void update(User user);

    void remove(User user);

    List<User> getVips();

}
