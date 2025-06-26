package com.rpgen.pokemon;

import java.util.Map;
import java.util.HashMap;

public class Nature {
    private final String id;
    private final String name;
    private final String increasedStat;
    private final String decreasedStat;
    private final String description;

    public Nature(String id, String name, String increasedStat, String decreasedStat, String description) {
        this.id = id;
        this.name = name;
        this.increasedStat = increasedStat;
        this.decreasedStat = decreasedStat;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIncreasedStat() {
        return increasedStat;
    }

    public String getDecreasedStat() {
        return decreasedStat;
    }

    public String getDescription() {
        return description;
    }

    public double getStatModifier(String stat) {
        if (increasedStat != null && increasedStat.equals(stat)) {
            return 1.1; // +10%
        } else if (decreasedStat != null && decreasedStat.equals(stat)) {
            return 0.9; // -10%
        }
        return 1.0; // Sin modificación
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("increasedStat", increasedStat);
        map.put("decreasedStat", decreasedStat);
        map.put("description", description);
        
        // Agregar modificadores específicos para cada estadística
        map.put("attackModifier", getStatModifier("attack"));
        map.put("defenseModifier", getStatModifier("defense"));
        map.put("specialAttackModifier", getStatModifier("special_attack"));
        map.put("specialDefenseModifier", getStatModifier("special_defense"));
        map.put("speedModifier", getStatModifier("speed"));
        
        return map;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", name, description);
    }
} 