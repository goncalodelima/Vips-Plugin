package com.minecraftsolutions.vip.model.key.adapter;

import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;
import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.key.Key;
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
            String time = (String) databaseQuery.get("time");

            if (name == null)
                throw new RuntimeException("name is null on query adapter");

            Vip vip = plugin.getVipService().get(identifier);
            if (vip == null)
                throw new RuntimeException("vip is null on query adapter (A key was created with a VIP that was later removed. Delete the database or remove the keys containing the removed VIP from the database and then restart the server)");

            long longTime;
            try {
                longTime = Long.parseLong(time);
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }

            Key key = new Key(name, vip, longTime);
            keys.add(key);

        } while (databaseQuery.next());

        return keys;
    }

}
