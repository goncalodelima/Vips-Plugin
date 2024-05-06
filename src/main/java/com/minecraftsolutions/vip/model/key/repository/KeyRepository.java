package com.minecraftsolutions.vip.model.key.repository;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.key.Key;
import com.minecraftsolutions.vip.model.key.adapter.KeyAdapter;
import com.minecraftsolutions.vip.model.key.adapter.QueryAdapter;

import java.util.List;

public class KeyRepository implements KeyFoundationRepository {

    private final Database database;
    private final KeyAdapter adapter;
    private final QueryAdapter queryAdapter;

    public KeyRepository(VipPlugin plugin, Database database) {
        this.database = database;
        this.adapter = new KeyAdapter(plugin);
        this.queryAdapter = new QueryAdapter(plugin);
        setup();
    }
    
    @Override
    public void setup() {
        database
                .execute("CREATE TABLE IF NOT EXISTS vip_key (name VARCHAR(10) PRIMARY KEY, vip TEXT NOT NULL, time LONG NOT NULL)")
                .write();
    }

    @Override
    public void insert(Key key) {
        database
                .execute("INSERT INTO vip_key VALUES(?,?,?)")
                .write(statement -> {
                    statement.set(1, key.getName());
                    statement.set(2, key.getVip().getIdentifier());
                    statement.set(3, key.getTime());
                });
    }

    @Override
    public void remove(Key key) {
        database
                .execute("DELETE FROM vip_key WHERE name = ?")
                .write(statement -> statement.set(1, key.getName()));
    }

    @Override
    public Key findOne(String name) {
        return database
                .execute("SELECT * FROM vip_key WHERE name = ? COLLATE utf8mb4_bin")
                .readOneWithAdapter(statement -> statement.set(1, name), adapter)
                .join();
    }

    @Override
    public List<Key> findAll() {
        return database
                .execute("SELECT * FROM vip_key")
                .readOneWithAdapter(statement -> {}, queryAdapter)
                .join();
    }


}
