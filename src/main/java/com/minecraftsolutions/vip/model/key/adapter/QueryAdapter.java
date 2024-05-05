package com.minecraftsolutions.vip.model.key.adapter;

import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.key.Key;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.vip.Vip;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class QueryAdapter implements DatabaseAdapter<List<Key>> {

    private VipPlugin plugin;

    @SneakyThrows
    @Override
    public List<Key> adapt(DatabaseQuery databaseQuery) {

        List<Key> keys = new ArrayList<>();

        do {

            String name = (String) databaseQuery.get("name");
            String identifier = (String) databaseQuery.get("vip");
            long time = (long) databaseQuery.get("time");

            Vip vip = plugin.getVipService().get(identifier);
            if (vip == null)
                throw new RuntimeException("vip is null on adapter (A key was created with a VIP that was later removed. Delete the database or remove the keys containing the removed VIP from the database and then restart the server)");

            Key key = new Key(name, vip, time);
            keys.add(key);

        } while (databaseQuery.next());

        return keys;
    }

}
