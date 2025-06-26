package com.rpgen.pokemon;

import com.rpgen.core.entity.Entity;
import com.rpgen.core.action.GameAction;
import com.rpgen.core.action.DamageMove;
import com.rpgen.core.action.StatusMove;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;
import java.util.HashMap;

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
    
    // Nuevas características
    private List<Ability> abilities;
    private Ability selectedAbility;
    private HeldItem heldItem;
    private Nature nature;
    private Stats stats;
    private int level = 50; // Nivel por defecto para combate

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
        
        // Inicializar nuevas características
        this.abilities = new ArrayList<>();
        this.stats = new Stats(maxHealth/2, attack, defense, specialAttack, specialDefense, speed);
        this.nature = NatureDatabase.getRandomNature();
        this.heldItem = null;
        this.selectedAbility = null;
        
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
        return calculateFinalHP();
    }

    @Override
    public int getAttack() {
        return calculateFinalStat("attack");
    }

    @Override
    public int getDefense() {
        return calculateFinalStat("defense");
    }

    public List<String> getTypes() {
        return types;
    }

    @Override
    public int getSpeed() {
        return calculateFinalStat("speed");
    }

    public int getSpecialAttack() {
        return calculateFinalStat("special_attack");
    }

    public int getSpecialDefense() {
        return calculateFinalStat("special_defense");
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

    // Nuevos getters y setters
    public List<Ability> getAbilities() {
        return new ArrayList<>(abilities);
    }

    public void setAbilities(List<Ability> abilities) {
        this.abilities = new ArrayList<>(abilities);
        if (!abilities.isEmpty() && selectedAbility == null) {
            selectedAbility = abilities.get(0);
        }
    }

    public Ability getSelectedAbility() {
        return selectedAbility;
    }

    public void setSelectedAbility(Ability ability) {
        if (abilities.contains(ability)) {
            this.selectedAbility = ability;
        }
    }

    public HeldItem getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(HeldItem item) {
        this.heldItem = item;
    }

    public Nature getNature() {
        return nature;
    }

    public void setNature(Nature nature) {
        this.nature = nature;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(100, level));
    }

    // Métodos para calcular estadísticas finales
    private int calculateFinalHP() {
        int baseHP = stats.calculateFinalHP(level);
        double itemModifier = heldItem != null ? heldItem.getStatModifier("hp") : 1.0;
        return (int) (baseHP * itemModifier);
    }

    private int calculateFinalStat(String stat) {
        double natureModifier = nature != null ? nature.getStatModifier(stat) : 1.0;
        int baseStat = stats.calculateFinalStat(stat, level, natureModifier);
        
        // Aplicar modificadores de objeto
        double itemModifier = 1.0;
        if (heldItem != null) {
            itemModifier = heldItem.getStatModifier(stat);
        }
        
        return (int) (baseStat * itemModifier);
    }

    @Override
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }

    @Override
    public void heal(int amount) {
        health = Math.min(getMaxHealth(), health + amount);
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
        return String.format("%s #%s\nHP: %d/%d\nAtaque: %d\nDefensa: %d\nVelocidad: %d\nAtaque Especial: %d\nDefensa Especial: %d\nTipos: %s\nNaturaleza: %s\nObjeto: %s\nHabilidad: %s",
            name, id, health, getMaxHealth(), getAttack(), getDefense(), getSpeed(), getSpecialAttack(), getSpecialDefense(), 
            String.join(", ", types), 
            nature != null ? nature.getName() : "Ninguna",
            heldItem != null ? heldItem.getName() : "Ninguno",
            selectedAbility != null ? selectedAbility.getName() : "Ninguna");
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

    // Método para obtener todos los datos del Pokémon como mapa
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("health", health);
        map.put("maxHealth", getMaxHealth());
        map.put("attack", getAttack());
        map.put("defense", getDefense());
        map.put("speed", getSpeed());
        map.put("specialAttack", getSpecialAttack());
        map.put("specialDefense", getSpecialDefense());
        map.put("types", types);
        map.put("imageUrl", imageUrl);
        map.put("moves", moves);
        map.put("selectedMoveIndices", selectedMoveIndices);
        map.put("status", status);
        map.put("level", level);
        
        // Nuevas características
        map.put("abilities", abilities.stream().map(Ability::toMap).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
        map.put("selectedAbility", selectedAbility != null ? selectedAbility.toMap() : null);
        map.put("heldItem", heldItem != null ? heldItem.toMap() : null);
        map.put("nature", nature != null ? nature.toMap() : null);
        map.put("stats", stats != null ? stats.toMap() : null);
        
        return map;
    }
} 