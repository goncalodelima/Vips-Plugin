package com.minecraftsolutions.vip.model.user.repository;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.user.adapter.VipAdapter;
import com.minecraftsolutions.vip.model.user.adapter.UserAdapter;
import org.json.simple.parser.JSONParser;

import java.util.List;

public class UserRepository implements UserFoundationRepository {

    private final Database database;
    private final UserAdapter adapter;
    private final VipAdapter vipAdapter;

    public UserRepository(VipPlugin plugin, Database database) {
        JSONParser parser = new JSONParser();
        this.database = database;
        this.adapter = new UserAdapter(plugin, parser);
        this.vipAdapter = new VipAdapter(plugin, parser);
        setup();
    }

    @Override
    public void setup() {
        database
                .execute("CREATE TABLE IF NOT EXISTS vip_user (name VARCHAR(16) PRIMARY KEY, enabledVip TEXT)")
                .write();

        database
                .execute("CREATE TABLE IF NOT EXISTS vip_time (name VARCHAR(16) REFERENCES vip_user(name), vip TEXT NOT NULL, time BIGINT, PRIMARY KEY(name, vip))")
                .write();
    }

    @Override
    public void insert(User user) {
        database
                .execute("INSERT INTO vip_user VALUES(?,?,?)")
                .write(statement -> {
                    statement.set(1, user.getName());
                    statement.set(2, user.getEnabledVip() == null ? null : user.getEnabledVip().getIdentifier());
                });
    }

    @Override
    public void update(User user) {

        database
                .execute("UPDATE vip_user SET enabledVip = ? WHERE name = ?")
                .write(statement -> {
                    statement.set(1, user.getEnabledVip() == null ? null : user.getEnabledVip().getIdentifier());
                    statement.set(3, user.getName());
                });

        user.getTime().forEach((vip, time) -> database
                .execute("INSERT INTO vip_time (name, vip, time) VALUES(?,?,?) ON DUPLICATE KEY UPDATE time = VALUES(time)")
                .write(statement -> {
                    statement.set(1, user.getName());
                    statement.set(2, vip.getName());
                    statement.set(3, time);
                }));

    }

    @Override
    public void updateVip(User user) {
        database
                .execute("UPDATE vip_user SET enabledVip = ? WHERE name = ?")
                .write(statement -> {
                    statement.set(1, user.getEnabledVip() == null ? null : user.getEnabledVip().getIdentifier());
                    statement.set(3, user.getName());
                });
    }

    @Override
    public void updateTime(User user) {

        user.getTime().forEach((vip, time) -> database
                .execute("INSERT INTO vip_time (name, vip, time) VALUES(?,?,?) ON DUPLICATE KEY UPDATE time = VALUES(time)")
                .write(statement -> {
                    statement.set(1, user.getName());
                    statement.set(2, vip.getName());
                    statement.set(3, time);
                }));

    }

    @Override
    public User findOne(String name) {
        return database
                .execute("SELECT * FROM vip_user WHERE name = ?")
                .readOneWithAdapter(statement -> statement.set(1, name), adapter)
                .join();
    }

    public List<User> findVips() {
        return database
                .execute("SELECT * FROM vip_user WHERE enabledVip IS NOT NULL")
                .readOneWithAdapter(statement -> {}, vipAdapter)
                .join();
    }


}
