package com.minecraftsolutions.vip.runnable;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;

@AllArgsConstructor
public class VipRunnable extends BukkitRunnable {

    private VipPlugin plugin;

    @Override
    public void run() {

        if (plugin.isFreeze() || plugin.getUserService().getVips() == null) {
            return;
        }

        for (User user : plugin.getUserService().getVips()) {
            long newValue = user.getTime().getOrDefault(user.getEnabledVip(), 0L) - 900000;
            if (newValue <= 0) {

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(user.getName());

                if (plugin.getJda() != null) {
                    plugin.getJda().removeDiscordRoles(offlinePlayer.getUniqueId(), Collections.singleton(user.getEnabledVip()));
                }

                user.getTime().remove(user.getEnabledVip());
                user.setEnabledVip(null);

                if (offlinePlayer.isOnline()) {
                    offlinePlayer.getPlayer().sendMessage(plugin.getMessage().getConfig().getString("expiredVip").replace("&", "§"));
                }

            } else {
                user.getTime().put(user.getEnabledVip(), newValue);
                plugin.getUserService().update(user);
            }
        }

    }

}
