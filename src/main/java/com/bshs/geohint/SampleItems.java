package com.bshs.geohint;

import com.bshs.geohint.model.RockLayer;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/** 지층 표본 아이템 생성/식별 (블록을 캤을 때 지급되는, 이름이 바뀐 아이템) */
public class SampleItems {

    public static NamespacedKey layerKey(GeoHintPlugin plugin) {
        return new NamespacedKey(plugin, "geo_layer");
    }

    public static ItemStack create(GeoHintPlugin plugin, RockLayer layer) {
        ItemStack item = new ItemStack(layer.primary);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "지질 시료 - " + ChatColor.YELLOW
                + layer.koreanName + ChatColor.GOLD + " [" + layer.name() + "층]");
        meta.setLore(List.of(
                ChatColor.GRAY + "미상의 지질 시대",
                ChatColor.DARK_GRAY + "/hint 또는 화석으로 연대를 추정해보자"
        ));
        meta.getPersistentDataContainer().set(layerKey(plugin), PersistentDataType.STRING, layer.name());
        item.setItemMeta(meta);
        return item;
    }

    /** 아이템이 지층 표본이면 해당 RockLayer, 아니면 null */
    public static RockLayer layerOf(GeoHintPlugin plugin, ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        String key = meta.getPersistentDataContainer().get(layerKey(plugin), PersistentDataType.STRING);
        if (key == null) return null;
        return RockLayer.fromKey(key);
    }
}
