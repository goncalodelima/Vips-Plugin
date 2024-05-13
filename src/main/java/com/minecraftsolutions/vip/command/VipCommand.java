package com.minecraftsolutions.vip.command;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.key.Key;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.vip.Vip;
import com.minecraftsolutions.vip.util.BukkitUtils;
import com.minecraftsolutions.vip.util.TimeUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
public class VipCommand implements CommandExecutor {

    private final VipPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if (sender instanceof Player && (args.length == 0 || !sender.hasPermission("ms-vip.admin"))) {

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
                plugin.getMessage().getConfig().getStringList("noVipEnabled").forEach(string -> sender.sendMessage(string.replace("&", "§").replace("%vips%", user.getTime().keySet().toString())));
                return true;
            }

            Vip enabledVip = user.getEnabledVip();
            long time = user.getTime().get(enabledVip);

            plugin.getMessage().getConfig().getStringList("remainingTime").forEach(string -> {

                if (!string.contains("%listVips%")) {
                    sender.sendMessage(string.replace("&", "§")
                            .replace("%time%", TimeUtils.format(time)));
                } else {
                    for (Vip vip : user.getTime().keySet()) {
                        sender.sendMessage(plugin.getMessage().getConfig().getString("listVips").replace("&", "§").replace("%vip%", vip.getName().replace("&", "§")).replace("%time%", TimeUtils.format(user.getTime().get(enabledVip))));
                    }
                }

            });
            return true;
        }

        if (!(sender instanceof Player) && args.length == 0) {
            plugin.getMessage().getConfig().getStringList("help").forEach(string -> sender.sendMessage(string.replace("&", "§")));
            return false;
        }

        if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("congelar")) {

            if (plugin.isFreeze()) {
                plugin.setFreeze(false);
                sender.sendMessage(plugin.getMessage().getConfig().getString("unfreeze").replace("&", "§"));
            } else {
                plugin.setFreeze(true);
                sender.sendMessage(plugin.getMessage().getConfig().getString("freeze").replace("&", "§"));
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {

            if (args.length != 4) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("giveSyntax").replace("&", "§"));
                return false;
            }

            String identifier = args[1];
            Vip vip = plugin.getVipService().get(identifier);

            if (vip == null) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("invalidIdentifier").replace("&", "§"));
                return false;
            }

            String playerName = args[2];
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
            Optional<User> optionalTarget = plugin.getUserService().get(targetPlayer.getName());

            if (!targetPlayer.hasPlayedBefore() || !optionalTarget.isPresent()) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("invalidPlayer").replace("&", "§"));
                return false;
            }

            if (plugin.getConfig().getBoolean("onlineGive") && !targetPlayer.isOnline()) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("offlinePlayer").replace("&", "§"));
                return false;
            }

            int days;
            try {
                days = Integer.parseInt(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("invalidNumber").replace("&", "§"));
                return false;
            }

            if (days <= 0) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("invalidNumber").replace("&", "§"));
                return false;
            }

            long maxMillis = Long.MAX_VALUE / TimeUtils.DAY.getMillis();
            long time = days > maxMillis ? Long.MAX_VALUE : TimeUtils.DAY.getMillis() * days; //check if TimeUtils.DAY.getMillis() * days > Long.MAX_VALUE (handle problems)

            User targetUser = optionalTarget.get();

            long sum = targetUser.getTime().getOrDefault(vip, (long) 0);

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

            targetUser.getTime().put(vip, newTime);
            targetUser.setEnabledVip(vip);

            plugin.getUserService().update(targetUser);

            if (plugin.getJda() != null) {
                plugin.getJda().addDiscordRole(targetPlayer.getUniqueId(), vip);
                plugin.getJda().sendMessage(targetPlayer.getName(), vip);
            }

            vip.getCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("&", "§").replace("%identifier%", vip.getIdentifier())));

            sender.sendMessage(plugin.getMessage().getConfig().getString("successGive").replace("&", "§").replace("%targetName%", targetPlayer.getName()));

            if (targetPlayer.isOnline()) {
                targetPlayer.getPlayer().sendMessage(plugin.getMessage().getConfig().getString("receive").replace("&", "§").replace("%color%", vip.getColor().replace("&", "§")).replace("%vipName%", vip.getName().replace("&", "§")));
            }

            if (plugin.getConfig().getBoolean("chat")) {
                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> plugin.getMessage().getConfig().getStringList("chat").forEach(string -> sender.sendMessage(string.replace("&", "§").replace("%color%", vip.getColor().replace("&", "§")).replace("%targetName%", targetPlayer.getName()).replace("%vipName%", vip.getName().replace("&", "§")))));
            }

            if (plugin.getConfig().getBoolean("title")) {
                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> BukkitUtils.sendTitle(onlinePlayer, plugin.getMessage().getConfig().getString("title").replace("&", "§").replace("%color%", vip.getColor().replace("&", "§")).replace("%targetName%", targetPlayer.getName()).replace("%vipName%", vip.getName().replace("&", "§")), plugin.getMessage().getConfig().getString("subtitle").replace("&", "§").replace("%color%", vip.getColor().replace("&", "§")).replace("%vipName%", vip.getName().replace("&", "§")), 10, 20, 10));
            }

            if (plugin.getConfig().getBoolean("actionBar")) {
                Bukkit.getOnlinePlayers().forEach(onlinePlayer -> BukkitUtils.sendActionBar(onlinePlayer, plugin.getMessage().getConfig().getString("actionBar").replace("&", "§").replace("%color%", vip.getColor().replace("&", "§")).replace("%targetName%", targetPlayer.getName()).replace("%vipName%", vip.getName().replace("&", "§"))));
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {

            if (args.length != 3) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("removeSyntax").replace("&", "§"));
                return false;
            }

            String playerName = args[2];
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
            Optional<User> optionalTarget = plugin.getUserService().get(targetPlayer.getName());

            if (!targetPlayer.hasPlayedBefore() || !optionalTarget.isPresent()) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("invalidPlayer").replace("&", "§"));
                return false;
            }

            User targetUser = optionalTarget.get();
            String identifier = args[1];

            if (!identifier.equalsIgnoreCase("clear")) {
                Vip vip = plugin.getVipService().get(identifier);

                if (vip == null) {
                    sender.sendMessage(plugin.getMessage().getConfig().getString("invalidIdentifier").replace("&", "§"));
                    return false;
                }

                if (!targetUser.getTime().containsKey(vip)) {
                    sender.sendMessage(plugin.getMessage().getConfig().getString("containsVip").replace("&", "§"));
                    return false;
                }

                if (plugin.getJda() != null) {
                    plugin.getJda().removeDiscordRoles(Bukkit.getOfflinePlayer(targetUser.getName()).getUniqueId(), Collections.singleton(targetUser.getEnabledVip()));
                }

                targetUser.getTime().put(vip, 0L);

                if (targetPlayer.isOnline()) {
                    targetPlayer.getPlayer().sendMessage(plugin.getMessage().getConfig().getString("remove").replace("&", "§"));
                }

                sender.sendMessage(plugin.getMessage().getConfig().getString("successRemove").replace("&", "§").replace("%targetName%", targetPlayer.getName()));

            } else {

                if (plugin.getJda() != null) {
                    plugin.getJda().removeDiscordRoles(Bukkit.getOfflinePlayer(targetUser.getName()).getUniqueId(), targetUser.getTime().keySet());
                }

                targetUser.getTime().clear();

                if (targetPlayer.isOnline()) {
                    targetPlayer.getPlayer().sendMessage(plugin.getMessage().getConfig().getString("removeAll").replace("&", "§"));
                }

                sender.sendMessage(plugin.getMessage().getConfig().getString("successClear").replace("&", "§").replace("%targetName%", targetPlayer.getName()));

            }

            targetUser.setEnabledVip(null);
            plugin.getUserService().update(targetUser);
            return true;
        }

        if (args[0].equalsIgnoreCase("genkey") || args[0].equalsIgnoreCase("gerarkey")) {

            if (args.length != 3) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("genSyntax").replace("&", "§"));
                return false;
            }

            Vip vip = plugin.getVipService().get(args[1]);

            if (vip == null) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("invalidIdentifier").replace("&", "§"));
                return false;
            }

            int days;
            try {
                days = Integer.parseInt(args[2]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("invalidNumber").replace("&", "§"));
                return false;
            }

            if (days <= 0) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("invalidNumber").replace("&", "§"));
                return false;
            }

            long maxMillis = Long.MAX_VALUE / TimeUtils.DAY.getMillis();
            long time = days > maxMillis ? Long.MAX_VALUE : TimeUtils.DAY.getMillis() * days; //check if TimeUtils.DAY.getMillis() * days > Long.MAX_VALUE (handle exception)
            String name = RandomStringUtils.randomAlphabetic(10);

            plugin.getKeyService().put(new Key(name, vip, time));
            sender.sendMessage(plugin.getMessage().getConfig().getString("genkeySuccess").replace("&", "§").replace("%identifier%", vip.getIdentifier()).replace("%key%", name));
            return true;
        }

        if (args[0].equalsIgnoreCase("removekey") || args[0].equalsIgnoreCase("remkey") || args[0].equalsIgnoreCase("removerkey")) {

            if (args.length != 2) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("removekeySyntax").replace("&", "§"));
                return false;
            }

            Optional<Key> optionalKey = plugin.getKeyService().get(args[1]);
            if (!optionalKey.isPresent()) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("invalidKey").replace("&", "§"));
                return false;
            }

            plugin.getKeyService().remove(optionalKey.get());
            sender.sendMessage(plugin.getMessage().getConfig().getString("removekeySuccess").replace("&", "§"));
            return true;
        }

        if (args[0].equalsIgnoreCase("keylist") || args[0].equalsIgnoreCase("keyslist") || args[0].equalsIgnoreCase("listakeys") || args[0].equalsIgnoreCase("listarkeys") || args[0].equalsIgnoreCase("listarkey") || args[0].equalsIgnoreCase("listakey")) {

            Set<Key> keys = plugin.getKeyService().getAll();

            if (keys.isEmpty()) {
                sender.sendMessage(plugin.getMessage().getConfig().getString("emptyKeys").replace("&", "§"));
            } else {
                for (Key key : keys) {
                    plugin.getMessage().getConfig().getStringList("listKeys").forEach(string -> sender.sendMessage(string.replace("&", "§").replace("%key%", key.getName()).replace("%duration%", TimeUtils.format(key.getTime())).replace("%vipName%", key.getVip().getIdentifier())));
                }
            }

            return true;
        }

        plugin.getMessage().getConfig().getStringList("help").forEach(string -> sender.sendMessage(string.replace("&", "§")));
        return false;
    }

}
