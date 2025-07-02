package com.rpgen.chrono;

public interface ChronoBattleListener {
    void onBattleStart();
    void onBattleEnd(boolean playerVictory);
    void onEntityReady(ChronoEntity entity);
    void onActionExecuted(ChronoEntity actor, ChronoMove move, ChronoEntity target);
    void onStatusChanged(ChronoEntity entity, ChronoStatus status, boolean applied);
} 