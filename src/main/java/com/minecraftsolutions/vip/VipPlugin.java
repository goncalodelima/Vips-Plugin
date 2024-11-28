package com.minecraftsolutions.vip;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.database.DatabaseType;
import com.minecraftsolutions.database.connection.DatabaseConnection;
import com.minecraftsolutions.database.credentials.impl.DatabaseCredentialsImpl;
import com.minecraftsolutions.vip.command.ChangeVipCommand;
import com.minecraftsolutions.vip.command.TimeVipCommand;
import com.minecraftsolutions.vip.command.UseKeyCommand;
import com.minecraftsolutions.vip.command.VipCommand;
import com.minecraftsolutions.vip.listener.PlayerListener;
import com.minecraftsolutions.vip.model.key.service.KeyFoundationService;
import com.minecraftsolutions.vip.model.key.service.KeyService;
import com.minecraftsolutions.vip.model.user.User;
import com.minecraftsolutions.vip.model.user.service.UserFoundationService;
import com.minecraftsolutions.vip.model.user.service.UserService;
import com.minecraftsolutions.vip.model.vip.loader.VipLoader;
import com.minecraftsolutions.vip.model.vip.service.VipFoundationService;
import com.minecraftsolutions.vip.model.vip.service.VipService;
import com.minecraftsolutions.vip.runnable.UpdateRunnable;
import com.minecraftsolutions.vip.runnable.VipRunnable;
import com.minecraftsolutions.vip.util.jda.DiscordIntegration;
import com.minecraftsolutions.vip.util.configuration.Configuration;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
public class VipPlugin extends JavaPlugin {

    private ExecutorService asyncExecutor;

    private Configuration message;

    private Configuration vip;

    private Configuration discord;

    private Database datacenter;

    private VipFoundationService vipService;

    private KeyFoundationService keyService;

    private UserFoundationService userService;

    private DiscordIntegration jda;

    private boolean freeze;

    @Override
    public void onEnable() {

        asyncExecutor = Executors.newCachedThreadPool();

        setupConfigurations();
        getServer().getScheduler().runTaskLater(this, this::setupServices, 20L);
    }

    @Override
    public void onDisable() {

        Set<User> users = userService.getPendingUpdates();

        if (!users.isEmpty()) {
            userService.update(users).join();
        }

        datacenter.close();

        asyncExecutor.shutdown();

        try {
            // Wait for currently executing tasks to finish
            if (!asyncExecutor.awaitTermination(36, TimeUnit.SECONDS)) {
                // Force shutdown if tasks are not finished in the given time
                asyncExecutor.shutdownNow();
                // Wait for tasks to respond to being cancelled
                if (!asyncExecutor.awaitTermination(36, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate in the specified time.");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            asyncExecutor.shutdownNow();
        }

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
                jda = new DiscordIntegration(this);
            } catch (IllegalStateException | ClassNotFoundException e) {
                getServer().getLogger().warning(e.getMessage());
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        new Metrics(this, 22436);

        datacenter = new DatabaseConnection(
                new DatabaseCredentialsImpl(
                        DatabaseType.valueOf(getConfig().getString("database.type")),
                        getConfig().getString("database.host"),
                        getConfig().getString("database.port"),
                        getConfig().getString("database.database"),
                        getConfig().getString("database.user"),
                        getConfig().getString("database.password"),
                        getConfig().getString("database.type")
                )
        ).setup();

        vipService = new VipService();
        new VipLoader(vip.getConfig()).setup().forEach(vipService::put);

        keyService = new KeyService(this, datacenter);

        userService = new UserService(this, datacenter);

        freeze = getConfig().getBoolean("freeze");
        new VipRunnable(this).runTaskTimer(this, 20 * 60 * 15, 20 * 60 * 15);
        new UpdateRunnable(this).runTaskTimer(this, 20 * 15, 20 * 15);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginCommand("vip").setExecutor(new VipCommand(this));
        getServer().getPluginCommand("changevip").setExecutor(new ChangeVipCommand(this));
        getServer().getPluginCommand("timevip").setExecutor(new TimeVipCommand(this));
        getServer().getPluginCommand("usekey").setExecutor(new UseKeyCommand(this));

    }

    public void setFreeze(boolean freeze) {
        getConfig().set("freeze", freeze);
        saveConfig();
        this.freeze = freeze;
    }

}
