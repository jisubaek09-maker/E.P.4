package com.bshs.geohint.model;

/** 플레이어 한 명의 진행 상태 (메모리에만 보관, hintRemaining 만 파일에 저장) */
public class PlayerState {

    public static final int MAX_HINTS = 6;

    public int hintRemaining = MAX_HINTS;
    public boolean hintWaiting = false;   // /hint 직후, 다음 우클릭 한 번을 기다리는 중

    public boolean answerMode = false;    // /answer 이후 계속 유지되는 제출 모드
    public int answerProgress = 0;        // 다음에 맞혀야 할 RockLayer.CHRONOLOGICAL_ORDER 의 인덱스
    public boolean finished = false;      // 이미 8개를 다 맞혀 성공한 플레이어인가
}
