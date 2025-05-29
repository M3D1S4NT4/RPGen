package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;

public interface CombatCommand {
    void execute();
    Entity getSource();
    Entity getTarget();
    String getActionType();
    boolean canExecute();
} 