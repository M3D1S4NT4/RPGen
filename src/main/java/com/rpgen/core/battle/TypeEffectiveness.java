package com.rpgen.core.battle;

import java.util.*;

public class TypeEffectiveness {
    private static final Map<String, Map<String, Double>> TYPE_CHART = new HashMap<>();
    
    static {
        // Inicializar la tabla de tipos de manera compacta
        // Formato: "tipo": {"tipo_efectivo": multiplicador, ...}
        TYPE_CHART.put("normal", Map.of(
            "rock", 0.5, "ghost", 0.0, "steel", 0.5
        ));
        TYPE_CHART.put("fire", Map.of(
            "fire", 0.5, "water", 0.5, "grass", 2.0, "ice", 2.0, "bug", 2.0,
            "rock", 0.5, "dragon", 0.5, "steel", 2.0
        ));
        TYPE_CHART.put("water", Map.of(
            "fire", 2.0, "water", 0.5, "grass", 0.5, "ground", 2.0, "rock", 2.0,
            "dragon", 0.5
        ));
        TYPE_CHART.put("electric", Map.of(
            "water", 2.0, "electric", 0.5, "grass", 0.5, "ground", 0.0,
            "flying", 2.0, "dragon", 0.5
        ));
        TYPE_CHART.put("grass", Map.of(
            "fire", 0.5, "water", 2.0, "grass", 0.5, "poison", 0.5, "ground", 2.0,
            "flying", 0.5, "bug", 0.5, "rock", 2.0, "dragon", 0.5, "steel", 0.5
        ));
        TYPE_CHART.put("ice", Map.of(
            "fire", 0.5, "water", 0.5, "grass", 2.0, "ice", 0.5, "ground", 2.0,
            "flying", 2.0, "dragon", 2.0, "steel", 0.5
        ));
        TYPE_CHART.put("fighting", Map.ofEntries(
            Map.entry("normal", 2.0), Map.entry("ice", 2.0), Map.entry("poison", 0.5),
            Map.entry("flying", 0.5), Map.entry("psychic", 0.5), Map.entry("bug", 0.5),
            Map.entry("rock", 2.0), Map.entry("ghost", 0.0), Map.entry("dark", 2.0),
            Map.entry("steel", 2.0), Map.entry("fairy", 0.5)
        ));
        TYPE_CHART.put("poison", Map.of(
            "grass", 2.0, "poison", 0.5, "ground", 0.5, "rock", 0.5, "ghost", 0.5,
            "steel", 0.0, "fairy", 2.0
        ));
        TYPE_CHART.put("ground", Map.of(
            "fire", 2.0, "electric", 2.0, "grass", 0.5, "poison", 2.0, "flying", 0.0,
            "bug", 0.5, "rock", 2.0, "steel", 2.0
        ));
        TYPE_CHART.put("flying", Map.of(
            "electric", 0.5, "grass", 2.0, "fighting", 2.0, "bug", 2.0, "rock", 0.5,
            "steel", 0.5
        ));
        TYPE_CHART.put("psychic", Map.of(
            "fighting", 2.0, "poison", 2.0, "psychic", 0.5, "dark", 0.0, "steel", 0.5
        ));
        TYPE_CHART.put("bug", Map.ofEntries(
            Map.entry("fire", 0.5), Map.entry("grass", 2.0), Map.entry("fighting", 0.5),
            Map.entry("poison", 0.5), Map.entry("flying", 0.5), Map.entry("psychic", 2.0),
            Map.entry("ghost", 0.5), Map.entry("dark", 2.0), Map.entry("steel", 0.5),
            Map.entry("fairy", 0.5)
        ));
        TYPE_CHART.put("rock", Map.of(
            "fire", 2.0, "ice", 2.0, "fighting", 0.5, "ground", 0.5, "flying", 2.0,
            "bug", 2.0, "steel", 0.5
        ));
        TYPE_CHART.put("ghost", Map.of(
            "normal", 0.0, "psychic", 2.0, "ghost", 2.0, "dark", 0.5
        ));
        TYPE_CHART.put("dragon", Map.of(
            "dragon", 2.0, "steel", 0.5, "fairy", 0.0
        ));
        TYPE_CHART.put("dark", Map.of(
            "fighting", 0.5, "psychic", 2.0, "ghost", 2.0, "dark", 0.5, "fairy", 0.5
        ));
        TYPE_CHART.put("steel", Map.of(
            "fire", 0.5, "water", 0.5, "electric", 0.5, "ice", 2.0, "rock", 2.0,
            "steel", 0.5, "fairy", 2.0
        ));
        TYPE_CHART.put("fairy", Map.of(
            "fighting", 2.0, "poison", 0.5, "dragon", 2.0, "dark", 2.0, "steel", 0.5
        ));
    }

    public static double getEffectiveness(String attackType, List<String> defenderTypes) {
        double effectiveness = 1.0;
        
        for (String defenderType : defenderTypes) {
            Map<String, Double> typeRelations = TYPE_CHART.getOrDefault(attackType, Collections.emptyMap());
            effectiveness *= typeRelations.getOrDefault(defenderType, 1.0);
        }
        
        return effectiveness;
    }
} 