package com.minecraftsolutions.vip;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.vip.command.ChangeVipCommand;
import com.minecraftsolutions.vip.command.TimeVipCommand;
import com.minecraftsolutions.vip.command.UseKeyCommand;
import com.minecraftsolutions.vip.command.VipCommand;
import com.minecraftsolutions.vip.database.DatabaseProvider;
import com.minecraftsolutions.vip.listener.PlayerListener;
import com.minecraftsolutions.vip.model.key.service.KeyFoundationService;
import com.minecraftsolutions.vip.model.key.service.KeyService;
import com.minecraftsolutions.vip.model.user.service.UserFoundationService;
import com.minecraftsolutions.vip.model.user.service.UserService;
import com.minecraftsolutions.vip.model.vip.loader.VipLoader;
import com.minecraftsolutions.vip.model.vip.service.VipFoundationService;
import com.minecraftsolutions.vip.model.vip.service.VipService;
import com.minecraftsolutions.vip.runnable.VipRunnable;
import com.minecraftsolutions.vip.util.BotJDA;
import com.minecraftsolutions.vip.util.configuration.Configuration;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class VipPlugin extends JavaPlugin {

    private Configuration message;

    private Configuration vip;

    private Configuration discord;

    private Database datacenter;

    private VipFoundationService vipService;

    private KeyFoundationService keyService;

    private UserFoundationService userService;

    private BotJDA jda;

    @Setter
    private boolean freeze;

    @Override
    public void onEnable() {
        setupConfigurations();
        getServer().getScheduler().runTaskLater(this, this::setupServices, 20L);
    }

    @Override
    public void onDisable() {
        datacenter.close();
    }

    private void setupConfigurations() {

        saveDefaultConfig();

        message = new Configuration("message.yml", this);
        message.saveFile();

        vip = new Configuration("vip.yml", this);
        vip.saveFile();

        discord = new Configuration("discord.yml", this);
        discord.saveFile();

    }

    private void setupServices() {

        if (discord.getConfig().getBoolean("enable") && (discord.getConfig().getBoolean("notification") || discord.getConfig().getBoolean("role"))) {
            try {
                Class.forName("github.scarsz.discordsrv.DiscordSRV");
                jda = new BotJDA(this);
            } catch (IllegalStateException | ClassNotFoundException e) {
                getServer().getLogger().warning(e.getMessage());
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        datacenter = new DatabaseProvider().setup(this);

        vipService = new VipService();
        new VipLoader(vip.getConfig()).setup().forEach(vipService::put);

        keyService = new KeyService(this, datacenter);

        userService = new UserService(this, datacenter);

        new VipRunnable(this).runTaskTimer(this, 20 * 60 * 15, 20 * 60 * 15);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginCommand("vip").setExecutor(new VipCommand(this));
        getServer().getPluginCommand("changevip").setExecutor(new ChangeVipCommand(this));
        getServer().getPluginCommand("timevip").setExecutor(new TimeVipCommand(this));
        getServer().getPluginCommand("usekey").setExecutor(new UseKeyCommand(this));

    }

}
