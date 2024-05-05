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
import java.util.stream.Collectors;

@AllArgsConstructor
public class VipAdapter implements DatabaseAdapter<List<User>> {

    private VipPlugin plugin;
    private JSONParser parser;

    @SneakyThrows
    @Override
    public List<User> adapt(DatabaseQuery databaseQuery) {

        List<User> users = new ArrayList<>();

        do {

            String name = (String) databaseQuery.get("name");
            String vips = (String) databaseQuery.get("vips");

            List<Vip> vipList = Arrays.stream(vips.split(","))
                    .map(identifier -> plugin.getVipService().get(identifier))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            String enabledVipIdentifier = (String) databaseQuery.get("enabledVip");
            Vip enabledVip = plugin.getVipService().get(enabledVipIdentifier);

            String timeString = (String) databaseQuery.get("time");

            JSONObject jsonObject = (JSONObject) parser.parse(timeString);
            Map<Vip, Long> time = new HashMap<>();

            for (Object identifier : jsonObject.keySet()) {
                time.put(plugin.getVipService().get((String) identifier), (Long) jsonObject.get(identifier));
            }

            User user = new User(name, vipList, enabledVip, time);
            users.add(user);

        } while (databaseQuery.next());

        return users;
    }

}
