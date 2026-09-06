package com.bshs.geohint.model;

import org.bukkit.Material;

import java.util.Locale;

/**
 * 지질 단면 모델의 8개 층/사건.
 * age 는 수업용으로 확정한 "정답" 연대(백만 년 전, Ma)이며,
 * 부정합·습곡·단층 같은 사건은 층이 아니라 위/아래 층의 나이로 시기가 한정된다.
 *
 * 반감기/동위원소 수치는 계산 실습을 위해 단순화한 가상의 값이다.
 * (원소 이름도 실제 방사성 동위원소와 혼동되지 않도록 그리스 문자로 표기했다.
 *  실제 동위원소 이름을 쓰고 싶다면 element 필드만 바꾸면 된다.)
 *
 * 화석 2개(C, H)의 연대는 실제 국제층서위원회(ICS) GSSP 기준 값이다.
 *   C = Palmatolepis triangularis, 파메니안절 기저 (약 372 Ma)
 *   H = Declinognathodus noduliferus, 바쉬키르절 기저 (약 323 Ma)
 */
public enum RockLayer {

    A("이암(셰일)", Material.DEEPSLATE, Material.COBBLED_DEEPSLATE,
      380, "θ(세타)", 95.0, 4, false),

    C("석회암", Material.CALCITE, Material.POLISHED_DEEPSLATE,
      372, null, 0, 0, true),

    F("화강암", Material.GRANITE, null,
      366, "λ(람다)", 122.0, 3, false),

    D("사암", Material.SANDSTONE, Material.CUT_SANDSTONE,
      348, "ξ(크사이)", 174.0, 2, false),

    E("이암(적색층)", Material.TERRACOTTA, Material.ORANGE_TERRACOTTA,
      339, "π(파이)", 339.0, 1, false),

    G("응회암", Material.TUFF, Material.ANDESITE,
      331, "σ(시그마)", 165.5, 2, false),

    H("셰일", Material.MUD_BRICKS, Material.PACKED_MUD,
      323, null, 0, 0, true),

    // 관입암 B 는 /hint 도, 화석도 없다 - 나머지 7개 시기를 다 알아내면
    // 남는 시기 하나가 자동으로 정해지는 소거법 문제다.
    B("섬록암", Material.DIORITE, null,
      312, null, 0, 0, false);

    public final String koreanName;
    public final Material primary;
    public final Material secondary;   // 층리 줄무늬용 보조 블록 (없으면 null)
    public final int ageMa;
    public final String element;       // /hint 용, 없으면 null
    public final double halfLifeMa;
    public final int halfLivesElapsed; // n
    public final boolean fossil;       // 화석으로 연대를 추정하는 층인가

    RockLayer(String koreanName, Material primary, Material secondary,
              int ageMa, String element, double halfLifeMa, int halfLivesElapsed,
              boolean fossil) {
        this.koreanName = koreanName;
        this.primary = primary;
        this.secondary = secondary;
        this.ageMa = ageMa;
        this.element = element;
        this.halfLifeMa = halfLifeMa;
        this.halfLivesElapsed = halfLivesElapsed;
        this.fossil = fossil;
    }

    /** /hint 로 반감기 정보를 볼 수 있는 층인가 (A, F, D, E, G 5개) */
    public boolean hasIsotopeHint() {
        return element != null;
    }

    public double parentPercent() {
        return 100.0 * Math.pow(0.5, halfLivesElapsed);
    }

    public double daughterPercent() {
        return 100.0 - parentPercent();
    }

    /** 오래된 것(380Ma) → 젊은 것(312Ma) 순서. /answer 정답 순서. */
    public static final RockLayer[] CHRONOLOGICAL_ORDER = { A, C, F, D, E, G, H, B };

    public static RockLayer fromMaterial(Material m) {
        for (RockLayer layer : values()) {
            if (layer.primary == m || layer.secondary == m) {
                return layer;
            }
        }
        return null;
    }

    public static RockLayer fromKey(String key) {
        try {
            return valueOf(key.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
