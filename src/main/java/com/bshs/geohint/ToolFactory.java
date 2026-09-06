package com.bshs.geohint;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** 효율 10 · 파괴불가 네더라이트 삽/곡괭이 */
public class ToolFactory {

    public static void giveTools(Player player) {
        player.getInventory().addItem(make(Material.NETHERITE_PICKAXE));
        player.getInventory().addItem(make(Material.NETHERITE_SHOVEL));
    }

    private static ItemStack make(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true); // 내구성 무한

        // Registry 로 조회 - 마인크래프트/페이퍼 버전이 올라가며 정적 필드 이름이
        // 종종 바뀌기 때문에(예: DIG_SPEED -> EFFICIENCY) 네임스페이스 키로 찾는 편이
        // 더 안전하다. 26.2 에서 키 이름이 또 바뀌었다면 이 한 줄만 고치면 된다.
        Enchantment efficiency = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("efficiency"));
        if (efficiency != null) {
            meta.addEnchant(efficiency, 10, true); // 10 = 바닐라 최대(5)를 넘는 강화치, unsafe=true 로 허용
        }

        item.setItemMeta(meta);
        return item;
    }
}
