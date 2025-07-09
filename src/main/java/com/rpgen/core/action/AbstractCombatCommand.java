package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;

public abstract class AbstractCombatCommand implements CombatCommand {
    protected final Entity source;
    protected final Entity target;
    protected final String actionName;
    protected final GameAction action;

    public AbstractCombatCommand(Entity source, Entity target, String actionName) {
        this.source = source;
        this.target = target;
        this.actionName = actionName;
        this.action = null;
    }

    public AbstractCombatCommand(Entity source, Entity target, String actionName, GameAction action) {
        this.source = source;
        this.target = target;
        this.actionName = actionName;
        this.action = action;
    }

    @Override
    public Entity getSource() {
        return source;
    }

    @Override
    public Entity getTarget() {
        return target;
    }

    @Override
    public String getActionName() {
        return actionName;
    }

    @Override
    public GameAction getAction() {
        return action;
    }

    @Override
    public boolean canExecute() {
        return source != null && target != null && source.isAlive() && target.isAlive();
    }

    @Override
    public abstract void execute();
} 