package com.minecraftsolutions.vip.listener;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PlayerListener implements Listener {

    private final VipPlugin plugin;

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();

        String nickname = event.getPlayer().getName();
        Optional<User> optionalUser = plugin.getUserService().get(uuid);

        if (!optionalUser.isPresent()) {
            User user = new User(uuid, nickname, null, new HashMap<>());
            plugin.getUserService().put(user);
        } else {

            User user = optionalUser.get();

            if (!user.getName().equals(nickname)) {
                user.setName(nickname);
                plugin.getUserService().update(user);
            }

        }

    }

}
