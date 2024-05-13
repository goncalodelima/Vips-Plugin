package com.minecraftsolutions.vip.model.user.repository;

import com.minecraftsolutions.vip.model.user.User;

import java.util.List;
import java.util.Set;

public interface UserFoundationRepository {

    void setup();

    void insert(User user);

    void update(User user);

    void updateVip(User user);

    void updateTime(User user);

    User findOne(String name);

    Set<User> findVips();

}
