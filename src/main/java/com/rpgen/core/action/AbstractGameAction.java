package com.rpgen.core.action;


import com.rpgen.core.entity.Entity;

public abstract class AbstractGameAction implements GameAction {
    private final String id;
    private final String name;
    private final String description;
    private final int cooldown;

    protected AbstractGameAction(String id, String name, int cooldown, String description) {
        this.id = id;
        this.name = name;
        this.cooldown = cooldown;
        this.description = description;
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
    public boolean canExecute(Entity source, Entity target) {
        return source != null && target != null && 
               source.isAlive() && target.isAlive();
    }
} 