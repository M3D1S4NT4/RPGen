package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;
import java.util.Map;
import java.util.HashMap;

public abstract class PokemonMove implements GameAction {
    protected final String id;
    protected final String name;
    protected final String type;
    protected final String category;
    protected final int power;
    protected final int accuracy;
    protected final String description;
    protected final int cooldown;
    protected final Map<String, Object> properties;

    public PokemonMove(String id, String name, String type, String category, int power, int accuracy, String description, int cooldown) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.category = category;
        this.power = power;
        this.accuracy = accuracy;
        this.description = description;
        this.cooldown = cooldown;
        this.properties = new HashMap<>();
        properties.put("type", type);
        properties.put("category", category);
        properties.put("power", power);
        properties.put("accuracy", accuracy);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public boolean canExecute(Entity source, Entity target) {
        return source.isAlive() && target.isAlive();
    }
} 