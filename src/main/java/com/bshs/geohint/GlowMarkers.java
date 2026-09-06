package com.bshs.geohint;

import com.bshs.geohint.model.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

/**
 * 팀(건축물)마다 다른 색으로 빛나는(발광) 표식.
 * 투명·충돌없음 ArmorStand(마커) 1개를 각 팀 건축물 위에 세워두고,
 * 스코어보드 팀 색으로 항상 Glowing 처리한다.
 * Glowing 효과는 블록 뒤에서도 실루엣이 비쳐서, 서버 어디서든
 * "저기 우리 팀 건물이다 / 저 팀이 앞서 있다" 가 한눈에 보이게 된다.
 */
public class GlowMarkers {

    private static final String TEAM_PREFIX = "geo_region_";

    public static void spawn(GeoHintPlugin plugin, Region region) {
        World world = Bukkit.getWorld(region.world);
        if (world == null) {
            plugin.getLogger().warning("월드를 찾을 수 없습니다: " + region.world);
            return;
        }

        removeExisting(plugin, region.id);

        Location loc = new Location(world, region.x, region.y, region.z);
        ArmorStand stand = world.spawn(loc, ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setMarker(true);
            as.setInvulnerable(true);
            as.setSilent(true);
            as.setGravity(false);
            as.setCustomNameVisible(false);
            as.setGlowing(true);
            as.setPersistent(true);
        });

        ChatColor color;
        try {
            color = ChatColor.valueOf(region.color.toUpperCase());
        } catch (IllegalArgumentException ex) {
            color = ChatColor.WHITE;
        }

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = TEAM_PREFIX + region.id;
        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);
        team.setColor(color);
        team.addEntity(stand);

        // 나중에 다시 찾아서 지울 수 있도록 태그를 붙여둔다
        stand.addScoreboardTag("geo_marker_" + region.id);
    }

    public static void removeExisting(GeoHintPlugin plugin, int regionId) {
        String tag = "geo_marker_" + regionId;
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntitiesByClass(ArmorStand.class)) {
                if (e.getScoreboardTags().contains(tag)) {
                    e.remove();
                }
            }
        }
    }

    /** 서버(플러그인) 재시작 시 config 에 저장된 마커들을 다시 세운다 */
    public static void respawnAll(GeoHintPlugin plugin) {
        for (Region r : plugin.getDataStore().allRegions()) {
            spawn(plugin, r);
        }
    }
}
