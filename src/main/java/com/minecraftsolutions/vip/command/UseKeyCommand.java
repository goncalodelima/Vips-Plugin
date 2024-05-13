package com.minecraftsolutions.vip.command;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.key.Key;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.vip.Vip;
import com.minecraftsolutions.vip.util.BukkitUtils;
import com.minecraftsolutions.vip.util.TimeUtils;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

@AllArgsConstructor
public class UseKeyCommand implements CommandExecutor {

    private final VipPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if (!(sender instanceof Player))
            return false;

        if (args.length == 0) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("usekeySyntax").replace("&", "§"));
            return false;
        }

        Optional<User> optionalUser = plugin.getUserService().get(sender.getName());
        if (!optionalUser.isPresent()) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("error").replace("&", "§"));
            return false;
        }

        Optional<Key> optionalKey = plugin.getKeyService().get(args[0]);
        if (!optionalKey.isPresent()) {
            sender.sendMessage(plugin.getMessage().getConfig().getString("invalidKey").replace("&", "§"));
            return false;
        }

        Player player = (Player) sender;
        User user = optionalUser.get();

        Key key = optionalKey.get();
        Vip vip = key.getVip();

        long days = (key.getTime() / 1000 / 86400);
        long maxMillis = Long.MAX_VALUE / TimeUtils.DAY.getMillis();
        long time = days > maxMillis ? Long.MAX_VALUE : TimeUtils.DAY.getMillis() * days; //check if TimeUtils.DAY.getMillis() * days > Long.MAX_VALUE (handle problems)

        long sum = user.getTime().getOrDefault(vip, (long) 0);

        long newTime;
        if (time == Long.MAX_VALUE) {
            newTime = Long.MAX_VALUE;
        } else {
            long remainingTime = Long.MAX_VALUE - sum;
            if (remainingTime < time) {
                newTime = Long.MAX_VALUE;
            } else {
                newTime = time + sum;
            }
        }

        plugin.getKeyService().remove(key);
        user.getTime().put(vip, newTime);
        user.setEnabledVip(vip);

        plugin.getUserService().update(user);

        if (plugin.getJda() != null) {
            plugin.getJda().addDiscordRole(player.getUniqueId(), vip);
            plugin.getJda().sendMessage(user.getName(), vip);
        }

        vip.getCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("&", "§").replace("%identifier%", vip.getIdentifier())));

        player.sendMessage(plugin.getMessage().getConfig().getString("usedKey").replace("&", "§").replace("%color%", vip.getColor().replace("&", "§")).replace("%vipName%", vip.getName().replace("&", "§")));

        if (plugin.getConfig().getBoolean("chat")) {
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> plugin.getMessage().getConfig().getStringList("chat").forEach(string -> sender.sendMessage(string.replace("&", "§").replace("%color%", vip.getColor().replace("&", "§")).replace("%targetName%", player.getName()).replace("%vipName%", vip.getName().replace("&", "§")))));
        }

        if (plugin.getConfig().getBoolean("title")) {
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> BukkitUtils.sendTitle(onlinePlayer, plugin.getMessage().getConfig().getString("title").replace("&", "§").replace("%color%", vip.getColor().replace("&", "§")).replace("%targetName%", player.getName()).replace("%vipName%", vip.getName().replace("&", "§")), plugin.getMessage().getConfig().getString("subtitle").replace("&", "§").replace("%color%", vip.getColor().replace("&", "§")).replace("%vipName%", vip.getName().replace("&", "§")), 10, 20, 10));
        }

        if (plugin.getConfig().getBoolean("actionBar")) {
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> BukkitUtils.sendActionBar(onlinePlayer, plugin.getMessage().getConfig().getString("actionBar").replace("&", "§").replace("%color%", vip.getColor().replace("&", "§")).replace("%targetName%", player.getName()).replace("%vipName%", vip.getName().replace("&", "§"))));
        }

        return true;
    }

}