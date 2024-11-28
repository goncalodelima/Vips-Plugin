package com.minecraftsolutions.vip.model.user.repository;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.database.executor.DatabaseExecutor;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.user.adapter.UserAdapter;
import com.minecraftsolutions.vip.model.vip.service.VipFoundationService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserRepository implements UserFoundationRepository {

    private final Database database;
    private final UserAdapter adapter;
    private final Executor executor;
    private final Logger logger;

    public UserRepository(VipFoundationService vipService, Database database, Executor executor, Logger logger) {
        this.database = database;
        this.adapter = new UserAdapter(vipService);
        this.executor = executor;
        this.logger = logger;
        setup();
    }

    @Override
    public void setup() {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("CREATE TABLE IF NOT EXISTS vip_user (name VARCHAR(16) PRIMARY KEY, enabledVip TEXT)")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS vip_time (name VARCHAR(16) REFERENCES vip_user(name), vip VARCHAR(255) NOT NULL, time BIGINT, PRIMARY KEY(name, vip))")
                    .write();
        }
    }

    @Override
    public void insert(User user) {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("INSERT INTO vip_user VALUES(?,?)")
                    .write(statement -> {
                        statement.set(1, user.getName());
                        statement.set(2, user.getEnabledVip() == null ? null : user.getEnabledVip().getIdentifier());
                    });
        }
    }

    @Override
    public CompletableFuture<Void> update(Collection<User> users) {
        return CompletableFuture.runAsync(() -> {
            try (DatabaseExecutor executor = database.execute()) {

                executor.query("INSERT INTO vip_user (name, enabledVip) VALUES (?, ?) ON DUPLICATE KEY UPDATE enabledVip = VALUES(enabledVip)")
                        .batch(users, (user, statement) -> {
                            statement.set(1, user.getName());
                            statement.set(2, user.getEnabledVip() == null ? null : user.getEnabledVip().getIdentifier());
                        });

                Map<String, Map<String, Long>> userTimeMap = users.stream()
                        .collect(Collectors.toMap(
                                User::getName,
                                user -> user.getTime().entrySet().stream()
                                        .collect(Collectors.toMap(
                                                entry -> entry.getKey().getIdentifier(),
                                                Map.Entry::getValue,
                                                (existing, replacement) -> existing
                                        )),
                                (existing, replacement) -> {
                                    replacement.forEach((vip, time) -> existing.merge(vip, time, (oldTime, newTime) -> newTime));
                                    return existing;
                                }
                        ));

                executor.query("INSERT INTO vip_time (name, vip, time) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE time = VALUES(time)")
                        .batch(
                                userTimeMap.entrySet().stream()
                                        .flatMap(entry -> entry.getValue().entrySet().stream()
                                                .map(vipEntry -> new Object[]{
                                                        entry.getKey(),
                                                        vipEntry.getKey(),
                                                        vipEntry.getValue()
                                                })
                                        ).collect(Collectors.toList()), (params, statement) -> {
                                    statement.set(1, params[0]);
                                    statement.set(2, params[1]);
                                    statement.set(3, params[2]);
                                }
                        );

                Map<String, Set<String>> userVipToDelete = userTimeMap.entrySet().stream()
                        .filter(entry -> entry.getValue().values().stream().anyMatch(time -> time == 0))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().entrySet().stream()
                                        .filter(vipEntry -> vipEntry.getValue() == 0)
                                        .map(Map.Entry::getKey)
                                        .collect(Collectors.toSet())
                        ));

                executor.query("DELETE FROM vip_time WHERE name = ? AND vip = ?")
                        .batch(
                                userVipToDelete.entrySet().stream()
                                        .flatMap(entry -> entry.getValue().stream()
                                                .map(vip -> new Object[]{
                                                        entry.getKey(),
                                                        vip
                                                })
                                        ).collect(Collectors.toList()), (params, statement) -> {
                                    statement.set(1, params[0]);
                                    statement.set(2, params[1]);
                                }
                        );

            }
        }, executor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to update all vip data", e);
            return null;
        });
    }


    @Override
    public CompletableFuture<Boolean> update(User user) {

        return CompletableFuture.supplyAsync(() -> {
            try (DatabaseExecutor executor = database.execute()) {

                executor
                        .query("INSERT INTO vip_user (name, enabledVip) VALUES (?, ?) ON DUPLICATE KEY UPDATE enabledVip = VALUES(enabledVip)")
                        .write(statement -> {
                            statement.set(1, user.getName());
                            statement.set(2, user.getEnabledVip() == null ? null : user.getEnabledVip().getIdentifier());
                        });

                if (user.getTime().isEmpty()) {
                    executor
                            .query("DELETE FROM vip_time WHERE name = ?")
                            .write(statement -> statement.set(1, user.getName()));
                } else {

                    user.getTime().entrySet().removeIf(entry -> {

                        long time = entry.getValue();

                        if (time == 0) {

                            executor
                                    .query("DELETE FROM vip_time WHERE name = ? AND vip = ?")
                                    .write(statement -> {
                                        statement.set(1, user.getName());
                                        statement.set(2, entry.getKey().getIdentifier());
                                    });

                            return true;
                        } else {

                            executor
                                    .query("INSERT INTO vip_time (name, vip, time) VALUES(?,?,?) ON DUPLICATE KEY UPDATE time = VALUES(time)")
                                    .write(statement -> {
                                        statement.set(1, user.getName());
                                        statement.set(2, entry.getKey().getIdentifier());
                                        statement.set(3, time);
                                    });

                            return false;
                        }
                    });

                }
            }

            return true;
        }, executor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to update vip data", e);
            return false;
        });

    }

    @Override
    public Optional<User> findOne(String name) {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT * FROM vip_user A LEFT JOIN vip_time B ON A.name = B.name WHERE A.name = ?")
                    .readOne(statement -> statement.set(1, name), adapter);
        }
    }

    public Set<User> findVips() {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT A.*, B.vip, B.time FROM vip_user A LEFT JOIN vip_time B ON A.name = B.name")
                    .readMany(adapter, HashSet::new);
        }
    }

}
