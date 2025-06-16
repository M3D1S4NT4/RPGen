package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;

public class CombatCommandFactory {
    public static CombatCommand createCommand(Entity source, Entity target, String actionName) {
        return new AbstractCombatCommand(source, target, actionName) {
            @Override
            public void execute() {
                if (!canExecute()) return;
                int damage = Math.max(1, source.getAttack() - target.getDefense());
                target.takeDamage(damage);
            }
        };
    }

    public static CombatCommand createCommand(Entity source, Entity target, String actionName, GameAction action) {
        return new AbstractCombatCommand(source, target, actionName, action) {
            @Override
            public void execute() {
                if (!canExecute() || action == null) return;
                action.execute(source, target);
            }
        };
    }
} 