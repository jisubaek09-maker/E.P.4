package com.bshs.geohint.model;

/** 5개 팀 건축물 중 하나. 관전(경쟁심 유도) 발광 마커 위치를 등록해두기 위한 정보. */
public class Region {
    public int id;              // 1~5
    public String world;
    public double x, y, z;      // 발광 마커(ArmorStand) 위치
    public String color;        // ChatColor 이름 (RED, YELLOW, GREEN ...)

    public Region() {}

    public Region(int id, String world, double x, double y, double z, String color) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }
}
