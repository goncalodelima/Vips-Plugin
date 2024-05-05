package com.minecraftsolutions.vip.runnable;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;

@AllArgsConstructor
public class VipRunnable extends BukkitRunnable {

    private VipPlugin plugin;

    @Override
    public void run() {

        for (User user : plugin.getUserService().getVips()) {
            long newValue = user.getTime().getOrDefault(user.getEnabledVip(), 0L) - 900000;
            if (newValue <= 0) {

                if (plugin.getJda() != null) {
                    plugin.getJda().removeDiscordRoles(Bukkit.getOfflinePlayer(user.getName()).getUniqueId(), Collections.singletonList(user.getEnabledVip()));
                }

                user.getTime().remove(user.getEnabledVip());
                user.getVips().remove(user.getEnabledVip());
                user.setEnabledVip(null);

            } else {
                user.getTime().put(user.getEnabledVip(), newValue);
            }
        }

    }

}
