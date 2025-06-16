package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;
import java.util.Map;
import java.util.HashMap;

public class PredefinedActions {
    public static class BasicAttackAction extends AbstractGameAction {
        private static final int MIN_DAMAGE = 1;
        private final Map<String, Object> properties;

        public BasicAttackAction(String id) {
            super(id, "Ataque Básico", 0, "Un ataque básico que causa daño basado en el ataque del personaje");
            this.properties = new HashMap<>();
            properties.put("type", "normal");
            properties.put("category", "physical");
            properties.put("power", 40);
            properties.put("accuracy", 100);
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public void execute(Entity source, Entity target) {
            if (!canExecute(source, target)) {
                return;
            }
            
            int rawDamage = source.getAttack();
            int defense = target.getDefense();
            int finalDamage = Math.max(MIN_DAMAGE, rawDamage - defense);
            target.takeDamage(finalDamage);
        }
    }

    public static class StrongAttackAction extends AbstractGameAction {
        private static final int MIN_DAMAGE = 1;
        private static final double DAMAGE_MULTIPLIER = 2.0;
        private final Map<String, Object> properties;

        public StrongAttackAction(String id) {
            super(id, "Ataque Fuerte", 1, "Un ataque poderoso que causa el doble de daño");
            this.properties = new HashMap<>();
            properties.put("type", "normal");
            properties.put("category", "physical");
            properties.put("power", 80);
            properties.put("accuracy", 90);
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public void execute(Entity source, Entity target) {
            if (!canExecute(source, target)) {
                return;
            }
            
            int rawDamage = (int)(source.getAttack() * DAMAGE_MULTIPLIER);
            int defense = target.getDefense();
            int finalDamage = Math.max(MIN_DAMAGE, rawDamage - defense);
            target.takeDamage(finalDamage);
        }
    }

    public static class ShieldAction extends AbstractGameAction {
        private static final int SHIELD_DURATION = 1;
        private int remainingTurns;
        private final Map<String, Object> properties;

        public ShieldAction(String id) {
            super(id, "Escudo", 2, "Activa un escudo que reduce el daño recibido");
            this.remainingTurns = SHIELD_DURATION;
            this.properties = new HashMap<>();
            properties.put("type", "normal");
            properties.put("category", "status");
            properties.put("power", 0);
            properties.put("accuracy", 100);
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public void execute(Entity source, Entity target) {
            if (!canExecute(source, target)) {
                return;
            }
            
            source.setDefense(source.getDefense() * 2);
            remainingTurns--;
            
            if (remainingTurns <= 0) {
                source.setDefense(source.getDefense() / 2);
            }
        }

        @Override
        public boolean canExecute(Entity source, Entity target) {
            return super.canExecute(source, target) && remainingTurns > 0;
        }
    }

    public static GameAction createBasicAttack(String id) {
        return new BasicAttackAction(id);
    }

    public static GameAction createStrongAttack(String id) {
        return new StrongAttackAction(id);
    }

    public static GameAction createShield(String id) {
        return new ShieldAction(id);
    }
} 