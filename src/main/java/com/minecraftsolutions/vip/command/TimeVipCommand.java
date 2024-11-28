package com.minecraftsolutions.vip.command;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.vip.Vip;
import com.minecraftsolutions.vip.util.TimeUtils;
import lombok.AllArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TimeVipCommand implements CommandExecutor {

    private final VipPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Optional<User> optionalUser = plugin.getUserService().get(sender.getName());
        if (!optionalUser.isPresent()) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("error").replace("&", "§"));
            return false;
        }

        User user = optionalUser.get();

        if (user.getEnabledVip() == null && user.getTime().isEmpty()) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("noVip").replace("&", "§"));
            return true;
        } else if (user.getEnabledVip() == null) {
            plugin.getMessage().getConfig().getStringList("noVipEnabled").forEach(string -> sender.sendMessage(string.replace("&", "§").replace("%vips%", user.getTime().keySet().stream().map(vip -> vip.getName().replace("&", "§")).collect(Collectors.toList()).toString())));
            return true;
        }

        Vip enabledVip = user.getEnabledVip();
        long time = user.getTime().getOrDefault(enabledVip, 0L);

        plugin.getMessage().getConfig().getStringList("remainingTime").forEach(string -> {

            if (!string.contains("%listVips%")) {
                sender.sendMessage(string.replace("&", "§").replace("%time%", TimeUtils.format(time)));
            } else {
                for (Vip vip : user.getTime().keySet()) {
                    sender.sendMessage(plugin.getMessage().getConfig().getString("listVips").replace("&", "§").replace("%vip%", vip.getName().replace("&", "§")).replace("%time%", TimeUtils.format(user.getTime().getOrDefault(vip, 0L))));
                }
            }

        });

        return true;
    }

}
