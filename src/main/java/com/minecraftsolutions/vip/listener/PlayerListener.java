package com.minecraftsolutions.vip.listener;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@AllArgsConstructor
public class PlayerListener implements Listener {

    private final VipPlugin plugin;

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        Optional<User> optionalUser = plugin.getUserService().get(player.getName());

        if (!optionalUser.isPresent()) {
            plugin.getUserService().put(new User(player.getName(), new ArrayList<>(), null, new HashMap<>()));
        }

    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Optional<User> optionalUser = plugin.getUserService().get(event.getPlayer().getName());
        optionalUser.ifPresent(user -> plugin.getUserService().remove(user));
    }

}
