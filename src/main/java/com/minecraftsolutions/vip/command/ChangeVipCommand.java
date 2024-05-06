package com.minecraftsolutions.vip.command;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.vip.Vip;
import lombok.AllArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Optional;

@AllArgsConstructor
public class ChangeVipCommand implements CommandExecutor {

    private final VipPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        Optional<User> optionalUser = plugin.getUserService().get(sender.getName());
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

        user.setEnabledVip(vip);
        plugin.getUserService().update(user);
        sender.sendMessage(plugin.getMessage().getConfig().getString("changeSuccess").replace("&", "§"));
        return true;
    }

}
