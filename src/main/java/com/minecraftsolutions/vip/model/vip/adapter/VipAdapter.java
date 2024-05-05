package com.minecraftsolutions.vip.model.vip.adapter;

import com.minecraftsolutions.vip.model.vip.Vip;
import com.minecraftsolutions.vip.util.configuration.ConfigurationAdapter;
import org.bukkit.configuration.ConfigurationSection;

public class VipAdapter implements ConfigurationAdapter<Vip> {

    @Override
    public Vip adapt(ConfigurationSection section) {
        return new Vip(section.getString("identifier").toUpperCase(), section.getString("name"), section.getString("color"), section.getStringList("commands"), section.getString("roleId"));
    }

}
