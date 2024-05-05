package com.minecraftsolutions.vip.database;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.database.DatabaseCredentials;
import com.minecraftsolutions.database.DatabaseFactory;
import com.minecraftsolutions.database.configuration.DatabaseConfiguration;
import com.minecraftsolutions.database.configuration.provider.BukkitDatabaseConfiguration;
import com.minecraftsolutions.vip.VipPlugin;
import org.bukkit.configuration.ConfigurationSection;

public class DatabaseProvider {

    public Database setup(VipPlugin plugin) {

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("database");
        DatabaseConfiguration configuration = new BukkitDatabaseConfiguration(section);
        DatabaseCredentials credentials = configuration.build();

        return DatabaseFactory.getInstance().build(credentials);
    }

}
