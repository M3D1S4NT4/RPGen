package com.rpgen.core.entity;

public class Character implements Entity {
    private final String id;
    private final String name;
    private int health;
    private final int maxHealth;
    private final int attack;
    private final int defense;
    

    public Character(String id, String name, int maxHealth, int attack, int defense) {
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
    public boolean isDefeated() {
        return !isAlive();
    }

    @Override
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }

    @Override
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }
} 