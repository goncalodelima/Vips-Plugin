package com.minecraftsolutions.vip.command;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.events.PlayerVipChangedEvent;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.vip.Vip;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

@AllArgsConstructor
public class ChangeVipCommand implements CommandExecutor {

    private final VipPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Optional<User> optionalUser = plugin.getUserService().get(((Player) sender).getUniqueId());
        if (!optionalUser.isPresent()) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("error").replace("&", "§"));
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("changeVipSyntax").replace("&", "§"));
            return false;
        }

        User user = optionalUser.get();
        if (user.getTime().isEmpty() || user.getTime().size() == 1) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("insufficientVipAmount").replace("&", "§"));
            return false;
        }

        Vip vip = plugin.getVipService().get(args[0]);
        if (vip == null) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("invalidIdentifier").replace("&", "§"));
            return false;
        }

        if (user.getEnabledVip() == vip) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("hasEnabled").replace("&", "§"));
            return false;
        }

        if (!user.getTime().containsKey(vip)) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("personalContainsVip").replace("&", "§"));
            return false;
        }

        Vip enabledVip = user.getEnabledVip();

        user.setEnabledVip(vip);
        plugin.getUserService().update(user);

        if (enabledVip != null) {
            enabledVip.getRemoveCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("&", "§").replace("%identifier%", enabledVip.getIdentifier()).replace("%targetName%", sender.getName())));
        }

        vip.getSetCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("&", "§").replace("%identifier%", vip.getIdentifier()).replace("%targetName%", sender.getName())));

        Bukkit.getPluginManager().callEvent(new PlayerVipChangedEvent((Player) sender, vip));

        sender.sendMessage(plugin.getMessage().getConfig().getString("changeSuccess").replace("&", "§"));
        return true;
    }

}
