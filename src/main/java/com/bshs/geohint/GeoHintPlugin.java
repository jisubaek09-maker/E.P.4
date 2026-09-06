package com.bshs.geohint;

import com.bshs.geohint.commands.CommandManager;
import com.bshs.geohint.data.DataStore;
import com.bshs.geohint.listeners.BreakListener;
import com.bshs.geohint.listeners.InteractListener;
import com.bshs.geohint.listeners.JoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public class GeoHintPlugin extends JavaPlugin {

    private DataStore dataStore;

    @Override
    public void onEnable() {
        dataStore = new DataStore(this);

        CommandManager commands = new CommandManager(this);
        for (String name : new String[]{"help", "hint", "answer", "dm", "health", "hungry", "tools", "geo"}) {
            var cmd = getCommand(name);
            if (cmd != null) cmd.setExecutor(commands);
        }

        getServer().getPluginManager().registerEvents(new BreakListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        JoinListener.startKeepAliveTask(this);
        GlowMarkers.respawnAll(this);

        getLogger().info("GeoHint 활성화 완료 - 지층 " + com.bshs.geohint.model.RockLayer.values().length + "종 로드됨");
    }

    public DataStore getDataStore() {
        return dataStore;
    }
}
