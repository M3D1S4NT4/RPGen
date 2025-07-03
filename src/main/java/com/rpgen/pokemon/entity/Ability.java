package com.rpgen.pokemon.entity;

import java.util.Map;
import java.util.HashMap;

public class Ability {
    private final String id;
    private final String name;
    private final String description;
    private final String effect;
    private final boolean isHidden;

    public Ability(String id, String name, String description, String effect, boolean isHidden) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.effect = effect;
        this.isHidden = isHidden;
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

    public boolean isHidden() {
        return isHidden;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("effect", effect);
        map.put("isHidden", isHidden);
        return map;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", name, description);
    }
} 