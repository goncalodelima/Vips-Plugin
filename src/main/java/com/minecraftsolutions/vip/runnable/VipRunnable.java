package com.minecraftsolutions.vip.runnable;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.events.PlayerVipChangedEvent;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.vip.Vip;
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

            Vip enabledVip = user.getEnabledVip();

            if (enabledVip == null || user.getTime().getOrDefault(enabledVip, 0L) == -1) {
                return;
            }

            long newValue = user.getTime().getOrDefault(enabledVip, 0L) - 900000;

            if (newValue <= 0) {

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(user.getName());

                if (plugin.getJda() != null) {
                    plugin.getJda().removeDiscordRoles(offlinePlayer.getUniqueId(), Collections.singleton(enabledVip));
                }

                user.getTime().put(enabledVip, 0L);
                user.setEnabledVip(null);

                plugin.getUserService().update(user);

                enabledVip.getRemoveCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("&", "§").replace("%identifier%", enabledVip.getIdentifier()).replace("%targetName%", user.getName())));

                Bukkit.getPluginManager().callEvent(new PlayerVipChangedEvent(offlinePlayer, null));

                if (offlinePlayer.isOnline()) {
                    offlinePlayer.getPlayer().sendMessage(plugin.getMessage().getConfig().getString("expiredVip").replace("&", "§"));
                }

            } else {

                user.getTime().put(enabledVip, newValue);
                plugin.getUserService().update(user);

                if (plugin.getJda() != null) {

                    Player player = Bukkit.getPlayer(user.getName());

                    if (player != null) {
                        plugin.getJda().addDiscordRole(player, enabledVip);
                    }

                }

            }
        }

    }

}
