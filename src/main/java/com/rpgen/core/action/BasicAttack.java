package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;
import java.util.Map;
import java.util.HashMap;

public class BasicAttack extends AbstractGameAction {
    private final Map<String, Object> properties;

    public BasicAttack(String id) {
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
        
        int damage = Math.max(1, source.getAttack() - target.getDefense());
        target.takeDamage(damage);
    }
} 