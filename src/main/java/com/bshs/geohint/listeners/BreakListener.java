package com.bshs.geohint.listeners;

import com.bshs.geohint.GeoHintPlugin;
import com.bshs.geohint.SampleItems;
import com.bshs.geohint.model.RockLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 지층 블록을 캐면 원래 블록 대신, 이름이 붙은 "지질 시료" 아이템을 준다.
 * (블록 자체를 표본으로 쓰면 인벤토리에서 무슨 층인지 구분이 안 되기 때문)
 */
public class BreakListener implements Listener {

    private final GeoHintPlugin plugin;

    public BreakListener(GeoHintPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        RockLayer layer = RockLayer.fromMaterial(type);
        if (layer == null) return; // 접촉변성대, 흙, 잔디 등은 그냥 원래대로 캐진다

        Player player = event.getPlayer();
        event.setDropItems(false);
        ItemStack sample = SampleItems.create(plugin, layer);
        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), sample);
    }
}
