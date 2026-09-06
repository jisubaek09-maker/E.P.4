package com.bshs.geohint.commands;

import com.bshs.geohint.GeoHintPlugin;
import com.bshs.geohint.GlowMarkers;
import com.bshs.geohint.ToolFactory;
import com.bshs.geohint.listeners.JoinListener;
import com.bshs.geohint.model.PlayerState;
import com.bshs.geohint.model.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.stream.Collectors;

/** /help, /hint, /answer, /dm, /health, /hungry, /tools, /geo 를 전부 처리한다 */
public class CommandManager implements CommandExecutor {

    private final GeoHintPlugin plugin;

    public CommandManager(GeoHintPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "help":    return help(sender);
            case "hint":    return hint(sender);
            case "answer":  return answer(sender);
            case "dm":      return dm(sender, args);
            case "health":  return health(sender);
            case "hungry":  return hungry(sender);
            case "tools":   return tools(sender);
            case "geo":     return geo(sender, args);
            default:        return false;
        }
    }

    // ── /help ──

    private boolean help(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== GeoHint 명령어 =====");
        sender.sendMessage(ChatColor.YELLOW + "/hint" + ChatColor.GRAY
                + " - 절대연령 힌트 요청 (총 " + PlayerState.MAX_HINTS + "회, 화석층/관입암은 대상 아님)");
        sender.sendMessage(ChatColor.YELLOW + "/answer" + ChatColor.GRAY + " - 정답(지질 순서) 제출 모드 시작");
        sender.sendMessage(ChatColor.YELLOW + "/dm <닉네임> <내용>" + ChatColor.GRAY + " - 귓속말");
        sender.sendMessage(ChatColor.YELLOW + "/health" + ChatColor.GRAY + " - 체력 회복");
        sender.sendMessage(ChatColor.YELLOW + "/hungry" + ChatColor.GRAY + " - 허기 회복");
        sender.sendMessage(ChatColor.YELLOW + "/tools" + ChatColor.GRAY + " - 삽/곡괭이 재지급");
        if (sender.hasPermission("geohint.admin")) {
            sender.sendMessage(ChatColor.DARK_GRAY + "── 관리자 ──");
            sender.sendMessage(ChatColor.YELLOW + "/geo setregion <1-5> <색상>" + ChatColor.GRAY
                    + " - 서 있는 위치를 그 팀 건축물의 발광 표식 위치로 등록");
            sender.sendMessage(ChatColor.YELLOW + "/geo removeregion <1-5>" + ChatColor.GRAY + " - 표식 제거");
            sender.sendMessage(ChatColor.YELLOW + "/geo list" + ChatColor.GRAY + " - 등록된 팀 목록");
            sender.sendMessage(ChatColor.YELLOW + "/geo reload" + ChatColor.GRAY + " - 설정 다시 불러오기");
        }
        return true;
    }

    // ── /hint ──

    private boolean hint(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return true;

        PlayerState state = plugin.getDataStore().state(player);
        if (state.hintRemaining <= 0) {
            player.sendMessage(ChatColor.RED + "힌트를 모두 사용했습니다 (0/" + PlayerState.MAX_HINTS + ").");
            return true;
        }
        state.hintWaiting = true;
        player.sendMessage(ChatColor.AQUA + "연대를 확인할 암석을 우클릭해주세요.");
        return true;
    }

    // ── /answer ──

    private boolean answer(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return true;

        PlayerState state = plugin.getDataStore().state(player);
        int total = com.bshs.geohint.model.RockLayer.CHRONOLOGICAL_ORDER.length;

        if (state.finished) {
            player.sendMessage(ChatColor.GREEN + "이미 지층 추론에 성공했습니다!");
            return true;
        }
        state.answerMode = true;
        player.sendMessage(ChatColor.AQUA + "가장 오래된 시료부터 순서대로 우클릭하세요. ("
                + state.answerProgress + "/" + total + ")");
        return true;
    }

    // ── /dm ──

    private boolean dm(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "사용법: /dm <닉네임> <내용>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "'" + args[0] + "' 님을 찾을 수 없습니다.");
            return true;
        }
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        String senderName = (sender instanceof Player) ? sender.getName() : "콘솔";

        target.sendMessage(ChatColor.LIGHT_PURPLE + "[" + senderName + " → 나] " + ChatColor.RESET + message);
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "[나 → " + target.getName() + "] " + ChatColor.RESET + message);
        return true;
    }

    // ── /health, /hungry ──

    private boolean health(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        double max = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(max);
        player.sendMessage(ChatColor.GREEN + "체력을 회복했습니다.");
        return true;
    }

    private boolean hungry(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.sendMessage(ChatColor.GREEN + "허기를 채웠습니다.");
        return true;
    }

    // ── /tools ──

    private boolean tools(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        ToolFactory.giveTools(player);
        player.sendMessage(ChatColor.GREEN + "효율10 · 파괴불가 네더라이트 삽/곡괭이를 지급했습니다.");
        return true;
    }

    // ── /geo (관리자) ──

    private boolean geo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("geohint.admin")) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "사용법: /geo <setregion|removeregion|list|reload>");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "setregion": {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "사용법: /geo setregion <1-5> <색상: RED/YELLOW/GREEN/AQUA/LIGHT_PURPLE ...>");
                    return true;
                }
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "팀 번호는 숫자여야 합니다.");
                    return true;
                }
                String colorArg = args[2].toUpperCase(Locale.ROOT);
                try {
                    ChatColor.valueOf(colorArg);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "알 수 없는 색상입니다. 예: RED, GOLD, YELLOW, GREEN, AQUA, BLUE, LIGHT_PURPLE, WHITE");
                    return true;
                }
                Region region = new Region(id, player.getWorld().getName(),
                        player.getLocation().getX(),
                        player.getLocation().getY() + 3, // 발 밑이 아니라 머리 위쯤에 표식
                        player.getLocation().getZ(), colorArg);
                plugin.getDataStore().putRegion(region);
                GlowMarkers.spawn(plugin, region);
                sender.sendMessage(ChatColor.GREEN + "" + id + "번 팀 표식을 " + colorArg + " 색으로 여기에 세웠습니다.");
                return true;
            }
            case "removeregion": {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "사용법: /geo removeregion <1-5>");
                    return true;
                }
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "팀 번호는 숫자여야 합니다.");
                    return true;
                }
                plugin.getDataStore().removeRegion(id);
                GlowMarkers.removeExisting(plugin, id);
                sender.sendMessage(ChatColor.GREEN + "" + id + "번 팀 표식을 제거했습니다.");
                return true;
            }
            case "list": {
                var regions = plugin.getDataStore().allRegions();
                if (regions.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "등록된 팀이 없습니다.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "등록된 팀: " + regions.stream()
                        .map(r -> r.id + "번(" + r.color + ")")
                        .collect(Collectors.joining(", ")));
                return true;
            }
            case "reload": {
                plugin.getDataStore().load();
                GlowMarkers.respawnAll(plugin);
                sender.sendMessage(ChatColor.GREEN + "설정을 다시 불러왔습니다.");
                return true;
            }
            default:
                sender.sendMessage(ChatColor.RED + "사용법: /geo <setregion|removeregion|list|reload>");
                return true;
        }
    }

    // ── util ──

    private Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player p) return p;
        sender.sendMessage(ChatColor.RED + "이 명령어는 게임 안에서 플레이어만 사용할 수 있습니다.");
        return null;
    }
}
