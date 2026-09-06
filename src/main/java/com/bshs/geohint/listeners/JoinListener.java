package com.bshs.geohint.listeners;

import com.bshs.geohint.GeoHintPlugin;
import com.bshs.geohint.ToolFactory;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * - 입장 시 야간투시(주변 밝기 효과)를 걸어 횃불 없이도 잘 보이게 한다.
 *   (서버는 클라이언트의 감마 슬라이더 자체를 조작할 수 없어서, 사실상 동일한
 *    효과를 내는 표준적인 방법인 "파티클/아이콘 없는 무한 야간투시"를 사용했다.)
 * - 처음 입장할 때 효율10·파괴불가 네더라이트 삽/곡괭이를 한 벌 지급한다.
 */
public class JoinListener implements Listener {

    private static final int NIGHT_VISION_SECONDS = 999999; // 사실상 무한

    private final GeoHintPlugin plugin;

    public JoinListener(GeoHintPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        applyNightVision(player);

        NamespacedKey givenKey = new NamespacedKey(plugin, "geo_tools_given");
        boolean alreadyGiven = player.getPersistentDataContainer()
                .getOrDefault(givenKey, PersistentDataType.BYTE, (byte) 0) == (byte) 1;
        if (!alreadyGiven) {
            ToolFactory.giveTools(player);
            player.getPersistentDataContainer().set(givenKey, PersistentDataType.BYTE, (byte) 1);
        }
    }

    public static void applyNightVision(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.NIGHT_VISION,
                NIGHT_VISION_SECONDS * 20,
                0,
                true,   // ambient
                false,  // particles
                false   // icon
        ));
    }

    /** 우유를 마셔서 효과가 지워진 플레이어를 위해 주기적으로 다시 걸어준다 */
    public static void startKeepAliveTask(GeoHintPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                    applyNightVision(p);
                }
            }
        }, 20L * 30, 20L * 30); // 30초마다 점검
    }
}
