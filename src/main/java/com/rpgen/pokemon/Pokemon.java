package com.rpgen.pokemon;

import com.rpgen.core.entity.Entity;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;

public class Pokemon implements Entity {
    private final String id;
    private final String name;
    private int health;
    private final int maxHealth;
    private final int attack;
    private final int defense;
    private final List<String> types;
    private final int speed;
    private final int specialAttack;
    private final int specialDefense;
    private final String imageUrl;
    private int defenseBonus = 0;
    private List<Map<String, Object>> moves;
    private List<Integer> selectedMoveIndices;

    public Pokemon(String id, String name, int maxHealth, int attack, int defense, 
                  List<String> types, int speed, int specialAttack, int specialDefense, String imageUrl, List<Map<String, Object>> moves) {
        this.id = id;
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attack = attack;
        this.defense = defense;
        this.types = types;
        this.speed = speed;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.imageUrl = imageUrl;
        this.moves = moves;
        this.selectedMoveIndices = new ArrayList<>();
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

    public List<String> getTypes() {
        return types;
    }

    public int getSpeed() {
        return speed;
    }

    public int getSpecialAttack() {
        return specialAttack;
    }

    public int getSpecialDefense() {
        return specialDefense;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<Map<String, Object>> getMoves() {
        return moves;
    }

    public List<Integer> getSelectedMoveIndices() {
        return selectedMoveIndices;
    }

    public void setSelectedMoveIndices(List<Integer> indices) {
        this.selectedMoveIndices = indices;
    }

    @Override
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }

    @Override
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }

    @Override
    public void setDefense(int bonus) {
        this.defenseBonus = bonus;
    }

    @Override
    public boolean isDefeated() {
        return health <= 0;
    }

    @Override
    public String toString() {
        return String.format("%s #%s\nHP: %d/%d\nAtaque: %d\nDefensa: %d\nVelocidad: %d\nAtaque Especial: %d\nDefensa Especial: %d\nTipos: %s",
            name, id, health, maxHealth, attack, defense, speed, specialAttack, specialDefense, String.join(", ", types));
    }

    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pokemon pokemon = (Pokemon) o;
        return Objects.equals(id, pokemon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 