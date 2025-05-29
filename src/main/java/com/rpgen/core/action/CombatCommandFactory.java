package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;
import java.util.UUID;

public class CombatCommandFactory {
    public static CombatCommand createCommand(Entity source, Entity target, String actionType) {
        String actionId = UUID.randomUUID().toString();
        GameAction gameAction;

        switch (actionType.toLowerCase()) {
            case "basic":
                gameAction = PredefinedActions.createBasicAttack(actionId);
                break;
            case "strong":
                gameAction = PredefinedActions.createStrongAttack(actionId);
                break;
            case "shield":
                gameAction = PredefinedActions.createShield(actionId);
                break;
            default:
                throw new IllegalArgumentException("Tipo de acción no válido: " + actionType);
        }

        return new AbstractCombatCommand(source, target, actionType, gameAction) {};
    }
} 