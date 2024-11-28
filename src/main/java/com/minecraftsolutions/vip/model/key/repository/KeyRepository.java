package com.minecraftsolutions.vip.model.key.repository;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.database.executor.DatabaseExecutor;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.key.Key;
import com.minecraftsolutions.vip.model.key.adapter.KeyAdapter;

import java.util.HashSet;
import java.util.Set;

public class KeyRepository implements KeyFoundationRepository {

    private final Database database;
    private final KeyAdapter adapter;

    public KeyRepository(VipPlugin plugin, Database database) {
        this.database = database;
        this.adapter = new KeyAdapter(plugin);
        setup();
    }

    @Override
    public void setup() {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("CREATE TABLE IF NOT EXISTS vip_key (name VARCHAR(10) PRIMARY KEY, vip TEXT NOT NULL, time LONG NOT NULL)")
                    .write();
        }
    }

    @Override
    public void insert(Key key) {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("INSERT INTO vip_key VALUES(?,?,?)")
                    .write(statement -> {
                        statement.set(1, key.getName());
                        statement.set(2, key.getVip().getIdentifier());
                        statement.set(3, key.getTime());
                    });
        }
    }

    @Override
    public void remove(Key key) {

        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("DELETE FROM vip_key WHERE name = ?")
                    .write(statement -> statement.set(1, key.getName()));
        }
    }

    @Override
    public Key findOne(String name) {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT * FROM vip_key WHERE name = ? COLLATE utf8mb4_bin")
                    .readOne(statement -> statement.set(1, name), adapter).orElse(null);
        }
    }

    @Override
    public Set<Key> findAll() {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT * FROM vip_key")
                    .readMany(adapter, HashSet::new);
        }
    }


}
