package com.bshs.geohint.listeners;

import com.bshs.geohint.GeoHintPlugin;
import com.bshs.geohint.SampleItems;
import com.bshs.geohint.model.PlayerState;
import com.bshs.geohint.model.RockLayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

/**
 * /hint 직후, 또는 /answer 로 제출 모드에 들어간 플레이어의 "다음 우클릭 한 번"을
 * 가로채서 처리한다. (오프핸드 중복 발생을 막기 위해 주손(HAND)만 처리)
 */
public class InteractListener implements Listener {

    private static final String WRONG_ITEM_MSG =
            ChatColor.RED + "올바른 암석이 아닙니다. 다시 입력해주세요.";

    private final GeoHintPlugin plugin;

    public InteractListener(GeoHintPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action a = event.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        PlayerState state = plugin.getDataStore().state(player);

        if (state.hintWaiting) {
            event.setCancelled(true);
            state.hintWaiting = false; // 한 번 시도로 끝 (틀려도 /hint 를 다시 입력해야 함)
            handleHintClick(player, state, event.getItem());
            return;
        }

        if (state.answerMode && !state.finished) {
            event.setCancelled(true);
            handleAnswerClick(player, state, event.getItem());
        }
    }

    private void handleHintClick(Player player, PlayerState state, ItemStack item) {
        RockLayer layer = SampleItems.layerOf(plugin, item);
        if (layer == null || !layer.hasIsotopeHint()) {
            player.sendMessage(WRONG_ITEM_MSG);
            return;
        }

        state.hintRemaining--;
        plugin.getDataStore().saveHint(player);

        player.sendMessage(ChatColor.AQUA + "── [" + layer.name() + "층 / " + layer.koreanName + "] ──");
        player.sendMessage(ChatColor.AQUA + "구성원소: " + ChatColor.WHITE + layer.element);
        player.sendMessage(ChatColor.AQUA + "반감기: " + ChatColor.WHITE
                + String.format(Locale.KOREA, "%.1f", layer.halfLifeMa) + " Ma");
        player.sendMessage(ChatColor.AQUA + "모원소/자원소(%): " + ChatColor.WHITE
                + String.format(Locale.KOREA, "%.2f%% / %.2f%%", layer.parentPercent(), layer.daughterPercent()));
        player.sendMessage(ChatColor.GRAY + "(남은 힌트 " + state.hintRemaining + "/" + PlayerState.MAX_HINTS + ")");
    }

    private void handleAnswerClick(Player player, PlayerState state, ItemStack item) {
        RockLayer layer = SampleItems.layerOf(plugin, item);
        RockLayer expected = RockLayer.CHRONOLOGICAL_ORDER[state.answerProgress];

        if (layer == null || layer != expected) {
            player.sendMessage(WRONG_ITEM_MSG);
            return;
        }

        state.answerProgress++;
        int total = RockLayer.CHRONOLOGICAL_ORDER.length;

        if (state.answerProgress >= total) {
            state.finished = true;
            state.answerMode = false;
            Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD
                    + player.getName() + ChatColor.RESET + ChatColor.GOLD + "님이 지층 추론에 성공하였습니다!");
        } else {
            player.sendMessage(ChatColor.GREEN + "정답 확인 (" + state.answerProgress + "/" + total + ")"
                    + ChatColor.GRAY + " - 다음으로 오래된 시료를 우클릭하세요.");
        }
    }
}
