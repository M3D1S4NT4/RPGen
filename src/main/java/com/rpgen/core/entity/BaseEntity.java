package com.rpgen.core.entity;

import com.rpgen.core.action.GameAction;
import java.util.*;

public abstract class BaseEntity implements Entity {
    protected final String id;
    protected final String name;
    protected int health;
    protected final int maxHealth;
    protected final int attack;
    protected final int defense;
    protected final int speed;
    protected List<GameAction> availableActions;
    protected int defenseBonus;
    protected boolean shielded = false;

    protected BaseEntity(String id, String name, int maxHealth, int attack, int defense, int speed) {
        this.id = id;
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.availableActions = new ArrayList<>();
        this.defenseBonus = 0;
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
        return defense + defenseBonus;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }

    @Override
    public void takeDamage(int damage) {
        if (shielded) {
            shielded = false;
            return;
        }
        if (damage < 0) {
            throw new IllegalArgumentException("El daño no puede ser negativo");
        }
        health = Math.max(0, health - damage);
    }

    @Override
    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("La curación no puede ser negativa");
        }
        health = Math.min(maxHealth, health + amount);
    }

    @Override
    public List<GameAction> getAvailableActions() {
        return new ArrayList<>(availableActions);
    }

    @Override
    public void setDefense(int bonus) {
        this.defenseBonus = bonus;
    }

    @Override
    public boolean isDefeated() {
        return health <= 0;
    }

    public void addAction(GameAction action) {
        availableActions.add(action);
    }

    @Override
    public void setAvailableActions(List<GameAction> actions) {
        this.availableActions = new ArrayList<>(actions);
    }

    public void setShielded(boolean value) {
        this.shielded = value;
    }

    public boolean isShielded() {
        return shielded;
    }
} 