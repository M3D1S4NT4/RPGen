package com.rpgen.core.battle;

import com.rpgen.core.entity.Entity;
import com.rpgen.core.action.GameAction;


public interface BattleListener {
    void onBattleStart();
    void onTurnStart();
    void onActionExecuted(Entity source, Entity target, GameAction action);
    void onEntityDamaged(Entity entity, int damage);
    void onEntityHealed(Entity entity, int amount);
    void onEntityDefeated(Entity entity);
    void onBattleEnd();
} 