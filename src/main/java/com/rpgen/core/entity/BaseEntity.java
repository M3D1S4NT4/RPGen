package com.rpgen.core.entity;

public abstract class BaseEntity implements Entity {
    protected final String id;
    protected final String name;
    protected int health;
    protected final int maxHealth;
    protected final int attack;
    protected final int defense;
    

    protected BaseEntity(String id, String name, int maxHealth, int attack, int defense) {
        this.id = id;
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attack = attack;
        this.defense = defense;
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
    public int getHealth() {
        return health;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public int getAttack() {
        return attack;
    }

    @Override
    public int getDefense() {
        return defense;
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }

    @Override
    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("El daño no puede ser negativo");
        }
        // El daño se reduce según la defensa
        int actualDamage = Math.max(1, damage - defense);
        health = Math.max(0, health - actualDamage);
    }

    @Override
    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("La curación no puede ser negativa");
        }
        health = Math.min(maxHealth, health + amount);
    }
} 