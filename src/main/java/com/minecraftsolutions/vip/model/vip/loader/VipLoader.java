package com.minecraftsolutions.vip.model.vip.loader;

import com.minecraftsolutions.vip.VipPlugin;
import com.minecraftsolutions.vip.model.vip.Vip;
import com.minecraftsolutions.vip.model.vip.adapter.VipAdapter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class VipLoader {

    private final FileConfiguration configuration;
    private final VipAdapter adapter;

    public VipLoader(FileConfiguration configuration) {
        this.configuration = configuration;
        this.adapter = new VipAdapter();
    }

    public List<Vip> setup() {
        return configuration.getConfigurationSection("").getKeys(false)
                .stream()
                .map(key -> adapter.adapt(configuration))
                .collect(Collectors.toList());
    }


}
