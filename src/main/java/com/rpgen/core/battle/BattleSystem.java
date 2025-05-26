package com.rpgen.core.battle;

import com.rpgen.core.entity.Entity;
import com.rpgen.core.action.GameAction;
import java.util.List;


public interface BattleSystem {
    void initialize(List<Entity> team1, List<Entity> team2);
    void addAction(Entity source, Entity target, GameAction action);
    void processTurn();
    List<Entity> getActiveEntities();
    boolean isBattleOver();
    void registerBattleListener(BattleListener listener);
    void removeBattleListener(BattleListener listener);
} 