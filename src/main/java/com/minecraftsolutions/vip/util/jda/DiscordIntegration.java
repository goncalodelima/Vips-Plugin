package com.minecraftsolutions.vip.util.jda;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.vip.Vip;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.*;
import github.scarsz.discordsrv.dependencies.jda.api.events.ReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DiscordIntegration extends ListenerAdapter {

    private final VipPlugin plugin;
    private final JDA jda;

    public DiscordIntegration(VipPlugin plugin) throws IllegalStateException {
        this.plugin = plugin;
        this.jda = DiscordSRV.getPlugin().getJda();
        if (this.jda == null) {
            throw new IllegalStateException();
        }
    }

    public void onReady(@NotNull ReadyEvent event) {
        Bukkit.getConsoleSender().sendMessage("Discord Bot started successfully!");
    }

    public GuildChannel getChannel(long guildId, long channelId) {

        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
            return null;

        return guild.getGuildChannelById(channelId);
    }

    public void sendMessage(String playerName, Vip vip) {

        if (!plugin.getDiscord().getConfig().getBoolean("enable") || !plugin.getDiscord().getConfig().getBoolean("notification"))
            return;

        String channel = plugin.getDiscord().getConfig().getString("channelId");
        String guild = plugin.getDiscord().getConfig().getString("guildId");

        TextChannel textChannel = (TextChannel) getChannel(Long.parseLong(guild), Long.parseLong(channel));

        if (textChannel == null) {
            Bukkit.getLogger().warning("Discord channel id in discord.yml is invalid.");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        embed.setAuthor(plugin.getDiscord().getConfig().getString("embed.author"), plugin.getDiscord().getConfig().getString("embed.authorUrl").replace("%playerName%", playerName));
        embed.setThumbnail(plugin.getDiscord().getConfig().getString("embed.url").replace("%playerName%", playerName));
        embed.setDescription(plugin.getDiscord().getConfig().getString("embed.description").replace("%playerName%", playerName).replace("%identifier%", vip.getIdentifier()).replace("%identifierLowerCase%", vip.getIdentifier().toLowerCase()).replace("%identifierUpperCase%", vip.getIdentifier().toUpperCase()));
        embed.setFooter(plugin.getDiscord().getConfig().getString("embed.footer").replace("%date%", dateFormat.format(date)));
        embed.setColor(new Color(plugin.getDiscord().getConfig().getInt("embed.color.red"), plugin.getDiscord().getConfig().getInt("embed.color.green"), plugin.getDiscord().getConfig().getInt("embed.color.blue")));

        textChannel.sendMessageEmbeds(embed.build()).queue(message -> {
        });

    }

    public void addDiscordRole(OfflinePlayer offlinePlayer, Vip vip) {

        if (!plugin.getDiscord().getConfig().getBoolean("enable") || !plugin.getDiscord().getConfig().getBoolean("role"))
            return;

        Guild guild = jda.getGuildById(plugin.getDiscord().getConfig().getString("guildId"));

        if (guild == null)
            return;

        Role role = guild.getRoleById(vip.getRoleId());

        if (role != null) {

            String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(offlinePlayer.getUniqueId());

            if (discordId == null) {

                if (offlinePlayer.getPlayer() != null) {
                    offlinePlayer.getPlayer().sendMessage(plugin.getMessage().getConfig().getString("noDiscordId").replace("&", "§"));
                }

                return;
            }

            guild.addRoleToMember(discordId, role).queue();
        }

    }

    public void removeDiscordRoles(UUID uuid, Set<Vip> vips) {

        if (!plugin.getDiscord().getConfig().getBoolean("enable") || !plugin.getDiscord().getConfig().getBoolean("role"))
            return;

        Guild guild = jda.getGuildById(plugin.getDiscord().getConfig().getString("guildId"));

        if (guild == null)
            return;

        List<Role> roles = new ArrayList<>();

        for (Vip vip : vips) {
            Role role = guild.getRoleById(vip.getRoleId());
            if (role != null)
                roles.add(role);
        }

        for (Role role : roles) {
            for (Member member : guild.getMembersWithRoles(role)) {
                if (DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(uuid).equals(member.getId())) {
                    guild.removeRoleFromMember(member, role).queue();
                    return;
                }
            }
        }

    }

}
