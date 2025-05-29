package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;

public abstract class AbstractCombatCommand implements CombatCommand {
    protected final Entity source;
    protected final Entity target;
    protected final String actionType;
    protected final GameAction gameAction;

    protected AbstractCombatCommand(Entity source, Entity target, String actionType, GameAction gameAction) {
        this.source = source;
        this.target = target;
        this.actionType = actionType;
        this.gameAction = gameAction;
    }

    @Override
    public void execute() {
        if (canExecute()) {
            gameAction.execute(source, target);
        }
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
    public String getActionType() {
        return actionType;
    }

    @Override
    public boolean canExecute() {
        return source != null && target != null && 
               source.isAlive() && target.isAlive() &&
               gameAction.canExecute(source, target);
    }
} 