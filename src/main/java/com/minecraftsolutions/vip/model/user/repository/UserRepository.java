package com.minecraftsolutions.vip.model.user.repository;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.user.adapter.UserAdapter;

import java.util.Set;

public class UserRepository implements UserFoundationRepository {

    private final Database database;
    private final UserAdapter adapter;

    public UserRepository(VipPlugin plugin, Database database) {
        this.database = database;
        this.adapter = new UserAdapter(plugin);
        setup();
    }

    @Override
    public void setup() {
        database
                .execute("CREATE TABLE IF NOT EXISTS vip_user (name VARCHAR(16) PRIMARY KEY, enabledVip TEXT)")
                .write();

        database
                .execute("CREATE TABLE IF NOT EXISTS vip_time (name VARCHAR(16) REFERENCES vip_user(name), vip VARCHAR(255) NOT NULL, time BIGINT, PRIMARY KEY(name, vip))")
                .write();
    }

    @Override
    public void insert(User user) {
        database
                .execute("INSERT INTO vip_user VALUES(?,?)")
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
                    statement.set(2, user.getName());
                });

        if (user.getTime().isEmpty()) {
            database
                    .execute("DELETE FROM vip_time WHERE name = ?")
                    .write(statement -> statement.set(1, user.getName()));
        } else {

            user.getTime().entrySet().removeIf(entry -> {

                long time = entry.getValue();

                if (time == 0) {
                    database
                            .execute("DELETE FROM vip_time WHERE name = ? AND vip = ?")
                            .write(statement -> {
                                statement.set(1, user.getName());
                                statement.set(2, entry.getKey().getIdentifier());
                            });

                    return true;

                } else {
                    database
                            .execute("INSERT INTO vip_time (name, vip, time) VALUES(?,?,?) ON DUPLICATE KEY UPDATE time = VALUES(time)")
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

    @Override
    public void updateVip(User user) {
        database
                .execute("UPDATE vip_user SET enabledVip = ? WHERE name = ?")
                .write(statement -> {
                    statement.set(1, user.getEnabledVip() == null ? null : user.getEnabledVip().getIdentifier());
                    statement.set(2, user.getName());
                });
    }

    @Override
    public void updateTime(User user) {

        user.getTime().forEach((vip, time) -> database
                .execute("INSERT INTO vip_time (name, vip, time) VALUES(?,?,?) ON DUPLICATE KEY UPDATE time = VALUES(time)")
                .write(statement -> {
                    statement.set(1, user.getName());
                    statement.set(2, vip.getIdentifier());
                    statement.set(3, time);
                }));

    }

    @Override
    public User findOne(String name) {
        return database
                .execute("SELECT * FROM vip_user A LEFT JOIN vip_time B ON A.name = B.name WHERE A.name = ?")
                .readOneWithAdapter(statement -> statement.set(1, name), adapter)
                .join();
    }

    public Set<User> findVips() {
        return database
                .execute("SELECT A.*, B.vip, B.time FROM vip_user A LEFT JOIN vip_time B ON A.name = B.name WHERE A.enabledVip IS NOT NULL")
                .readManyWithAdapter(adapter)
                .join();
    }

}
