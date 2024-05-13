package com.minecraftsolutions.vip.model.user.adapter;

import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.vip.Vip;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.*;

@AllArgsConstructor
public class UserAdapter implements DatabaseAdapter<User> {

    private VipPlugin plugin;

    @SneakyThrows
    @Override
    public User adapt(DatabaseQuery databaseQuery) {

        String name = (String) databaseQuery.get("name");
        String enabledVipIdentifier = (String) databaseQuery.get("enabledVip");

        Vip enabledVip = plugin.getVipService().get(enabledVipIdentifier);

        Map<Vip, Long> time = new HashMap<>();

        do {
            if (databaseQuery.get("vip") == null || databaseQuery.get("time") == null)
                continue;

            time.put(plugin.getVipService().get((String) databaseQuery.get("vip")), (Long) databaseQuery.get("time"));
        }while (databaseQuery.next());

        return new User(name, enabledVip, time);
    }

}
