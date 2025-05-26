package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;

public class BasicAttack extends AbstractGameAction {
    public BasicAttack(String id) {
        super(id, "Ataque Básico", 0, "Un ataque básico que causa daño basado en el ataque del personaje");
    }

    @Override
    public void execute(Entity source, Entity target) {
        if (!canExecute(source, target)) {
            return;
        }
        

        int damage = Math.max(1, source.getAttack() - target.getDefense());
        target.takeDamage(damage);
    }
} 