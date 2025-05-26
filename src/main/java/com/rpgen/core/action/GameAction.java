package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;


public interface GameAction {
    String getId();
    String getName();
    String getDescription();
    int getCooldown();
    void execute(Entity source, Entity target);
    boolean canExecute(Entity source, Entity target);
} 