package com.rpgen.core.entity;


public interface Entity {
    String getId();
    String getName();
    int getHealth();
    int getMaxHealth();
    int getAttack();
    int getDefense();
    void setDefense(int defense);
    boolean isAlive();
    void takeDamage(int damage);
    void heal(int amount);
    boolean isDefeated();
} 