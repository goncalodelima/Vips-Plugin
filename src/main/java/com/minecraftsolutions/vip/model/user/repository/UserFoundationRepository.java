package com.minecraftsolutions.vip.model.user.repository;

import com.minecraftsolutions.vip.model.user.User;

import java.util.List;

public interface UserFoundationRepository {

    void setup();

    void insert(User user);

    void update(User user);

    User findOne(String name);

    List<User> findVips();

}
