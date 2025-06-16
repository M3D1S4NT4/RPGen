package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;

public interface CombatCommand {
    Entity getSource();
    Entity getTarget();
    String getActionName();
    GameAction getAction();
    void execute();
    boolean canExecute();
} 