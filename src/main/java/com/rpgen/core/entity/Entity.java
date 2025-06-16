package com.rpgen.core.entity;

import com.rpgen.core.action.GameAction;
import java.util.List;

public interface Entity {
    String getId();
    String getName();
    int getHealth();
    int getMaxHealth();
    int getAttack();
    int getDefense();
    int getSpeed();
    boolean isAlive();
    void takeDamage(int damage);
    void heal(int amount);
    void setDefense(int bonus);
    List<GameAction> getAvailableActions();
    void setAvailableActions(List<GameAction> actions);
    boolean isDefeated();
} 