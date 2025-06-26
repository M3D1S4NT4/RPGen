package com.rpgen.pokemon;

import java.util.*;

public class NatureDatabase {
    private static final Map<String, Nature> natures = new HashMap<>();
    
    static {
        initializeNatures();
    }
    
    private static void initializeNatures() {
        // Naturalezas que aumentan y disminuyen estadísticas
        natures.put("hardy", new Nature("hardy", "Fuerte", null, null, "No afecta las estadísticas"));
        natures.put("lonely", new Nature("lonely", "Huraña", "attack", "defense", "Aumenta el Ataque, disminuye la Defensa"));
        natures.put("brave", new Nature("brave", "Audaz", "attack", "speed", "Aumenta el Ataque, disminuye la Velocidad"));
        natures.put("adamant", new Nature("adamant", "Firme", "attack", "special_attack", "Aumenta el Ataque, disminuye el Ataque Especial"));
        natures.put("naughty", new Nature("naughty", "Pícara", "attack", "special_defense", "Aumenta el Ataque, disminuye la Defensa Especial"));
        natures.put("bold", new Nature("bold", "Osada", "defense", "attack", "Aumenta la Defensa, disminuye el Ataque"));
        natures.put("docile", new Nature("docile", "Dócil", null, null, "No afecta las estadísticas"));
        natures.put("relaxed", new Nature("relaxed", "Plácida", "defense", "speed", "Aumenta la Defensa, disminuye la Velocidad"));
        natures.put("impish", new Nature("impish", "Agitada", "defense", "special_attack", "Aumenta la Defensa, disminuye el Ataque Especial"));
        natures.put("lax", new Nature("lax", "Floja", "defense", "special_defense", "Aumenta la Defensa, disminuye la Defensa Especial"));
        natures.put("timid", new Nature("timid", "Miedosa", "speed", "attack", "Aumenta la Velocidad, disminuye el Ataque"));
        natures.put("hasty", new Nature("hasty", "Activa", "speed", "defense", "Aumenta la Velocidad, disminuye la Defensa"));
        natures.put("serious", new Nature("serious", "Seria", null, null, "No afecta las estadísticas"));
        natures.put("jolly", new Nature("jolly", "Alegre", "speed", "special_attack", "Aumenta la Velocidad, disminuye el Ataque Especial"));
        natures.put("naive", new Nature("naive", "Ingenua", "speed", "special_defense", "Aumenta la Velocidad, disminuye la Defensa Especial"));
        natures.put("modest", new Nature("modest", "Modesta", "special_attack", "attack", "Aumenta el Ataque Especial, disminuye el Ataque"));
        natures.put("mild", new Nature("mild", "Afable", "special_attack", "defense", "Aumenta el Ataque Especial, disminuye la Defensa"));
        natures.put("quiet", new Nature("quiet", "Tímida", "special_attack", "speed", "Aumenta el Ataque Especial, disminuye la Velocidad"));
        natures.put("bashful", new Nature("bashful", "Tímida", null, null, "No afecta las estadísticas"));
        natures.put("rash", new Nature("rash", "Alocada", "special_attack", "special_defense", "Aumenta el Ataque Especial, disminuye la Defensa Especial"));
        natures.put("calm", new Nature("calm", "Serena", "special_defense", "attack", "Aumenta la Defensa Especial, disminuye el Ataque"));
        natures.put("gentle", new Nature("gentle", "Amable", "special_defense", "defense", "Aumenta la Defensa Especial, disminuye la Defensa"));
        natures.put("sassy", new Nature("sassy", "Grosera", "special_defense", "speed", "Aumenta la Defensa Especial, disminuye la Velocidad"));
        natures.put("careful", new Nature("careful", "Cauta", "special_defense", "special_attack", "Aumenta la Defensa Especial, disminuye el Ataque Especial"));
        natures.put("quirky", new Nature("quirky", "Rara", null, null, "No afecta las estadísticas"));
    }
    
    public static Nature getNature(String id) {
        return natures.get(id.toLowerCase());
    }
    
    public static List<Nature> getAllNatures() {
        return new ArrayList<>(natures.values());
    }
    
    public static Nature getRandomNature() {
        List<Nature> natureList = new ArrayList<>(natures.values());
        return natureList.get(new Random().nextInt(natureList.size()));
    }
    
    public static Map<String, Object> getAllNaturesAsMap() {
        Map<String, Object> result = new HashMap<>();
        System.out.println("Obteniendo todas las naturalezas...");
        System.out.println("Número de naturalezas: " + natures.size());
        for (Nature nature : natures.values()) {
            System.out.println("Añadiendo naturaleza: " + nature.getId() + " - " + nature.getName());
            result.put(nature.getId(), nature.toMap());
        }
        System.out.println("Total de naturalezas en resultado: " + result.size());
        return result;
    }
} 