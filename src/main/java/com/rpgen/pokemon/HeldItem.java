package com.rpgen.pokemon;

import java.util.Map;
import java.util.HashMap;

public class HeldItem {
    private final String id;
    private final String name;
    private final String description;
    private final String effect;
    private final Map<String, Double> statModifiers;
    private final String category;

    public HeldItem(String id, String name, String description, String effect, Map<String, Double> statModifiers, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.effect = effect;
        this.statModifiers = statModifiers != null ? new HashMap<>(statModifiers) : new HashMap<>();
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEffect() {
        return effect;
    }

    public Map<String, Double> getStatModifiers() {
        return new HashMap<>(statModifiers);
    }

    public String getCategory() {
        return category;
    }

    public double getStatModifier(String stat) {
        return statModifiers.getOrDefault(stat, 1.0);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("effect", effect);
        map.put("statModifiers", statModifiers);
        map.put("category", category);
        return map;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", name, description);
    }
} 