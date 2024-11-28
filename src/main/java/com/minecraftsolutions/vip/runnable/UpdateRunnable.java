package com.minecraftsolutions.vip.runnable;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
public class UpdateRunnable extends BukkitRunnable {

    private VipPlugin plugin;

    @Override
    public void run() {

        Set<User> users = plugin.getUserService().getPendingUpdates();

        if (!users.isEmpty()) {
            plugin.getUserService().update(new HashSet<>(users));
            users.clear();
        }

    }

}
