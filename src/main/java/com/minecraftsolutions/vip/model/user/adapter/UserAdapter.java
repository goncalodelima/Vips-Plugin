package com.minecraftsolutions.vip.model.user.adapter;

import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.vip.Vip;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.*;

@AllArgsConstructor
public class UserAdapter implements DatabaseAdapter<User> {

    private VipPlugin plugin;
    private JSONParser parser;

    @SneakyThrows
    @Override
    public User adapt(DatabaseQuery databaseQuery) {

        String name = (String) databaseQuery.get("name");

        String enabledVipIdentifier = (String) databaseQuery.get("enabledVip");
        Vip enabledVip = plugin.getVipService().get(enabledVipIdentifier);

        String timeString = (String) databaseQuery.get("time");

        JSONObject jsonObject = (JSONObject) parser.parse(timeString);
        Map<Vip, Long> time = new HashMap<>();

        for (Object identifier : jsonObject.keySet()) {
            time.put(plugin.getVipService().get((String) identifier), (Long) jsonObject.get(identifier));
        }

        return new User(name, enabledVip, time);
    }

}
