package com.minecraftsolutions.vip.model.user.repository;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.user.adapter.VipAdapter;
import com.minecraftsolutions.vip.model.user.adapter.UserAdapter;
import com.minecraftsolutions.vip.model.vip.Vip;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.List;
import java.util.stream.Collectors;

public class UserRepository implements UserFoundationRepository {

    private final Database database;
    private final UserAdapter adapter;
    private final VipAdapter vipAdapter;

    public UserRepository(VipPlugin plugin, Database database) {
        JSONParser parser = new JSONParser();
        this.database = database;
        this.adapter = new UserAdapter(plugin, parser);
        this.vipAdapter = new VipAdapter(plugin, parser);
    }

    @Override
    public void setup() {
        database
                .execute("CREATE TABLE IF NOT EXITS vip_user (name VARCHAR(16) PRIMARY KEY, vips TEXT NOT NULL, enabledVip TEXT, time TEXT NOT NULL)")
                .write();
    }

    @Override
    public void insert(User user) {

        String vips = user.getVips().stream().map(Vip::getIdentifier).collect(Collectors.joining(","));

        JSONObject jsonObject = new JSONObject();
        user.getTime().forEach((vip, time) -> jsonObject.put(vip.getIdentifier(), time));

        database
                .execute("INSERT INTO vip_user VALUES(?,?,?,?)")
                .write(statement -> {
                    statement.set(1, user.getName());
                    statement.set(2, vips);
                    statement.set(3, user.getEnabledVip() == null ? null : user.getEnabledVip().getIdentifier());
                    statement.set(4, jsonObject.toJSONString());
                });

    }

    @Override
    public void update(User user) {

        String vips = user.getVips().stream().map(Vip::getIdentifier).collect(Collectors.joining(","));

        JSONObject jsonObject = new JSONObject();
        user.getTime().forEach((vip, time) -> jsonObject.put(vip.getIdentifier(), time));

        database
                .execute("UPDATE vip_user SET vips = ?, enabledVip = ?, time = ? WHERE name = ?")
                .write(statement -> {
                    statement.set(1, vips);
                    statement.set(2, user.getEnabledVip() == null ? null : user.getEnabledVip().getIdentifier());
                    statement.set(3, jsonObject.toJSONString());
                    statement.set(4, user.getName());
                });

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
