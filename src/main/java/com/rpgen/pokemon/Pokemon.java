package com.rpgen.pokemon;

import com.rpgen.core.entity.Entity;
import com.rpgen.core.action.GameAction;
import com.rpgen.core.action.DamageMove;
import com.rpgen.core.action.StatusMove;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;

public class Pokemon implements Entity {
    private final String id;
    private final String name;
    private int health;
    private final int maxHealth;
    private final int baseAttack;
    private final int baseDefense;
    private final List<String> types;
    private final int baseSpeed;
    private final int baseSpecialAttack;
    private final int baseSpecialDefense;
    private final String imageUrl;
    private int attackModifier = 0;
    private int defenseModifier = 0;
    private int speedModifier = 0;
    private int specialAttackModifier = 0;
    private int specialDefenseModifier = 0;
    private String status;
    private List<Map<String, Object>> moves;
    private List<Integer> selectedMoveIndices;
    private List<GameAction> availableActions;

    public Pokemon(String id, String name, int maxHealth, int attack, int defense, 
                  List<String> types, int speed, int specialAttack, int specialDefense, String imageUrl, List<Map<String, Object>> moves) {
        this.id = id;
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.baseAttack = attack;
        this.baseDefense = defense;
        this.types = types;
        this.baseSpeed = speed;
        this.baseSpecialAttack = specialAttack;
        this.baseSpecialDefense = specialDefense;
        this.imageUrl = imageUrl;
        this.moves = moves;
        this.selectedMoveIndices = new ArrayList<>();
        this.availableActions = new ArrayList<>();
        this.status = null;
        initializeMoves();
    }

    private void initializeMoves() {
        for (Map<String, Object> moveData : moves) {
            String id = (String) moveData.getOrDefault("id", "move_" + System.currentTimeMillis());
            String name = (String) moveData.getOrDefault("name", "Movimiento");
            String type = (String) moveData.getOrDefault("type", "normal");
            String category = (String) moveData.getOrDefault("category", "physical");
            int power = ((Number) moveData.getOrDefault("power", 40)).intValue();
            int accuracy = ((Number) moveData.getOrDefault("accuracy", 100)).intValue();
            String description = (String) moveData.getOrDefault("description", "Un movimiento básico");
            String statusEffect = (String) moveData.get("statusEffect");

            GameAction move;
            if (statusEffect != null) {
                move = new StatusMove(id, name, type, category, power, accuracy, description, statusEffect);
            } else {
                move = new DamageMove(id, name, type, category, power, accuracy, description);
            }
            availableActions.add(move);
        }
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
        return baseAttack + attackModifier;
    }

    @Override
    public int getDefense() {
        return baseDefense + defenseModifier;
    }

    public List<String> getTypes() {
        return types;
    }

    @Override
    public int getSpeed() {
        return baseSpeed + speedModifier;
    }

    public int getSpecialAttack() {
        return baseSpecialAttack + specialAttackModifier;
    }

    public int getSpecialDefense() {
        return baseSpecialDefense + specialDefenseModifier;
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
    public boolean isDefeated() {
        return health <= 0;
    }

    @Override
    public String toString() {
        return String.format("%s #%s\nHP: %d/%d\nAtaque: %d\nDefensa: %d\nVelocidad: %d\nAtaque Especial: %d\nDefensa Especial: %d\nTipos: %s",
            name, id, health, maxHealth, getAttack(), getDefense(), getSpeed(), getSpecialAttack(), getSpecialDefense(), String.join(", ", types));
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

    @Override
    public List<GameAction> getAvailableActions() {
        return new ArrayList<>(availableActions);
    }

    @Override
    public void setAvailableActions(List<GameAction> actions) {
        this.availableActions = new ArrayList<>(actions);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAttack(int attack) {
        this.attackModifier = attack - this.baseAttack;
    }

    public void setDefense(int defense) {
        this.defenseModifier = defense - this.baseDefense;
    }

    public void setSpeed(int speed) {
        this.speedModifier = speed - this.baseSpeed;
    }

    public void setSpecialAttack(int specialAttack) {
        this.specialAttackModifier = specialAttack - this.baseSpecialAttack;
    }

    public void setSpecialDefense(int specialDefense) {
        this.specialDefenseModifier = specialDefense - this.baseSpecialDefense;
    }
} 