package com.minecraftsolutions.vip.runnable;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;

@AllArgsConstructor
public class VipRunnable extends BukkitRunnable {

    private VipPlugin plugin;

    @Override
    public void run() {

        if (plugin.isFreeze()) {
            return;
        }

        for (User user : plugin.getUserService().getVips()) {

            long newValue = user.getTime().getOrDefault(user.getEnabledVip(), 0L) - 900000;
            if (newValue <= 0) {

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(user.getName());

                if (plugin.getJda() != null) {
                    plugin.getJda().removeDiscordRoles(offlinePlayer.getUniqueId(), Collections.singleton(user.getEnabledVip()));
                }

                user.getTime().put(user.getEnabledVip(), 0L);
                user.setEnabledVip(null);

                plugin.getUserService().update(user);

                if (offlinePlayer.isOnline()) {
                    offlinePlayer.getPlayer().sendMessage(plugin.getMessage().getConfig().getString("expiredVip").replace("&", "§"));
                }

            } else {

                user.getTime().put(user.getEnabledVip(), newValue);
                plugin.getUserService().updateTime(user);

                if (plugin.getJda() != null) {

                    Player player = Bukkit.getPlayer(user.getName());

                    if (player != null) {
                        plugin.getJda().addDiscordRole(player.getUniqueId(), user.getEnabledVip());
                    }

                }

            }
        }

    }

}
