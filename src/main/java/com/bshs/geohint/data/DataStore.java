package com.bshs.geohint.data;

import com.bshs.geohint.model.PlayerState;
import com.bshs.geohint.model.Region;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/** regions.yml (팀 건축물 5개) + playerdata.yml (남은 힌트 횟수) 저장/로딩 */
public class DataStore {

    private final JavaPlugin plugin;
    private final File regionsFile;
    private final File playerFile;
    private FileConfiguration playerConfig;

    private final Map<Integer, Region> regions = new HashMap<>();
    private final Map<UUID, PlayerState> states = new HashMap<>();

    public DataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.regionsFile = new File(plugin.getDataFolder(), "regions.yml");
        this.playerFile = new File(plugin.getDataFolder(), "playerdata.yml");
        load();
    }

    // ── regions ──

    public void load() {
        plugin.getDataFolder().mkdirs();
        regions.clear();
        if (regionsFile.exists()) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(regionsFile);
            for (String key : cfg.getKeys(false)) {
                int id;
                try {
                    id = Integer.parseInt(key);
                } catch (NumberFormatException ex) {
                    continue;
                }
                Region r = new Region();
                r.id = id;
                r.world = cfg.getString(key + ".world");
                r.x = cfg.getDouble(key + ".x");
                r.y = cfg.getDouble(key + ".y");
                r.z = cfg.getDouble(key + ".z");
                r.color = cfg.getString(key + ".color", "WHITE");
                regions.put(id, r);
            }
        }
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
    }

    public void saveRegions() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Region r : regions.values()) {
            String key = String.valueOf(r.id);
            cfg.set(key + ".world", r.world);
            cfg.set(key + ".x", r.x);
            cfg.set(key + ".y", r.y);
            cfg.set(key + ".z", r.z);
            cfg.set(key + ".color", r.color);
        }
        try {
            cfg.save(regionsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "regions.yml 저장 실패", e);
        }
    }

    public void putRegion(Region r) {
        regions.put(r.id, r);
        saveRegions();
    }

    public void removeRegion(int id) {
        regions.remove(id);
        saveRegions();
    }

    public List<Region> allRegions() {
        return new ArrayList<>(regions.values());
    }

    // ── player state ──

    public PlayerState state(Player p) {
        return states.computeIfAbsent(p.getUniqueId(), id -> {
            PlayerState s = new PlayerState();
            s.hintRemaining = playerConfig.getInt(id + ".hint", PlayerState.MAX_HINTS);
            return s;
        });
    }

    public void saveHint(Player p) {
        PlayerState s = states.get(p.getUniqueId());
        if (s == null) return;
        playerConfig.set(p.getUniqueId() + ".hint", s.hintRemaining);
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "playerdata.yml 저장 실패", e);
        }
    }
}
