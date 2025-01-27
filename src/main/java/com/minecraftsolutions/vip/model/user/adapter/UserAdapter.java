package com.minecraftsolutions.vip.model.user.adapter;

import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.vip.Vip;
import com.minecraftsolutions.vip.model.vip.service.VipFoundationService;
import com.minecraftsolutions.vip.util.UUIDConverter;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.*;

@AllArgsConstructor
public class UserAdapter implements DatabaseAdapter<User> {

    private VipFoundationService vipService;

    private final Map<String, User> vipMap = new HashMap<>();

    @SneakyThrows
    @Override
    public User adapt(DatabaseQuery databaseQuery) {

        UUID uuid = UUIDConverter.convert((byte[]) databaseQuery.get("uuid"));
        String name = (String) databaseQuery.get("name");
        String enabledVipIdentifier = (String) databaseQuery.get("enabledVip");

        User user = vipMap.computeIfAbsent(name, string -> {
            Vip enabledVip = enabledVipIdentifier == null ? null : vipService.get(enabledVipIdentifier);
            return new User(uuid, string, enabledVip, new HashMap<>());
        });

        if (databaseQuery.get("vip") != null && databaseQuery.get("time") != null) {
            user.getTime().put(vipService.get((String) databaseQuery.get("vip")), (Long) databaseQuery.get("time"));
        }

        return user;
    }

}
