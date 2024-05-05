package com.minecraftsolutions.vip;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.vip.command.ChangeVipCommand;
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
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

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

    @Override
    public void onEnable() {

        setupConfigurations();
        setupServices();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginCommand("vip").setExecutor(new VipCommand(this));
        getServer().getPluginCommand("changevip").setExecutor(new ChangeVipCommand(this));

    }

    @Override
    public void onDisable() {
        datacenter.close();
    }

    private void setupConfigurations() {

        saveDefaultConfig();

        message = new Configuration("message.yml", this);
        vip = new Configuration("vip.yml", this);
        discord = new Configuration("discord.yml", this);

    }

    private void setupServices() {

        if (discord.getConfig().getBoolean("enable") && (discord.getConfig().getBoolean("notification") || discord.getConfig().getBoolean("role"))) {
            try {
                jda = new BotJDA(this);
            } catch (LoginException e) {
                Bukkit.getLogger().warning(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }

        datacenter = new DatabaseProvider().setup(this);

        vipService = new VipService();
        new VipLoader(vip.getConfig()).setup().forEach(vipService::put);

        keyService = new KeyService(this, datacenter);

        userService = new UserService(this, datacenter);

        new VipRunnable(this).runTaskTimer(this, 900, 900);

    }

}
