package com.rpgen.pokemon.data;

import java.util.*;

import com.rpgen.pokemon.entity.Nature;

public class NatureDatabase {
    private static final Map<String, Nature> natures = new HashMap<>();
    
    static {
        initializeNatures();
    }
    
    private static void initializeNatures() {
        // Naturalezas que aumentan y disminuyen estadísticas
        natures.put("hardy", new Nature("hardy", "Fuerte", null, null, "Neutral"));
        natures.put("docile", new Nature("docile", "Dócil", null, null, "Neutral"));
        natures.put("serious", new Nature("serious", "Seria", null, null, "Neutral"));
        natures.put("bashful", new Nature("bashful", "Tímida", null, null, "Neutral"));
        natures.put("quirky", new Nature("quirky", "Rara", null, null, "Neutral"));

        natures.put("lonely", new Nature("lonely", "Huraña", "attack", "defense", "Ataque+, Defensa-"));
        natures.put("brave", new Nature("brave", "Audaz", "attack", "speed", "Ataque+, Velocidad-"));
        natures.put("adamant", new Nature("adamant", "Firme", "attack", "special_attack", "Ataque+, Ataque Especial-"));
        natures.put("naughty", new Nature("naughty", "Pícara", "attack", "special_defense", "Ataque+, Defensa Especial-"));

        natures.put("bold", new Nature("bold", "Osada", "defense", "attack", "Defensa+, Ataque-"));
        natures.put("relaxed", new Nature("relaxed", "Plácida", "defense", "speed", "Defensa+, Velocidad-"));
        natures.put("impish", new Nature("impish", "Agitada", "defense", "special_attack", "Defensa+, Ataque Especial-"));
        natures.put("lax", new Nature("lax", "Floja", "defense", "special_defense", "Defensa+, Defensa Especial-"));

        natures.put("modest", new Nature("modest", "Modesta", "special_attack", "attack", "Ataque Especial+, Ataque-"));
        natures.put("mild", new Nature("mild", "Afable", "special_attack", "defense", "Ataque Especial+, Defensa-"));
        natures.put("quiet", new Nature("quiet", "Tímida", "special_attack", "speed", "Ataque Especial+, Velocidad-"));
        natures.put("rash", new Nature("rash", "Alocada", "special_attack", "special_defense", "Ataque Especial+, Defensa Especial-"));

        natures.put("calm", new Nature("calm", "Serena", "special_defense", "attack", "Defensa Especial+, Ataque-"));
        natures.put("gentle", new Nature("gentle", "Amable", "special_defense", "defense", "Defensa Especial+, Defensa-"));
        natures.put("sassy", new Nature("sassy", "Grosera", "special_defense", "speed", "Defensa Especial+, Velocidad-"));
        natures.put("careful", new Nature("careful", "Cauta", "special_defense", "special_attack", "Defensa Especial+, Ataque Especial-"));

        natures.put("timid", new Nature("timid", "Miedosa", "speed", "attack", "Velocidad+, Ataque-"));
        natures.put("hasty", new Nature("hasty", "Activa", "speed", "defense", "Velocidad+, Defensa-"));
        natures.put("jolly", new Nature("jolly", "Alegre", "speed", "special_attack", "Velocidad+, Ataque Especial-"));
        natures.put("naive", new Nature("naive", "Ingenua", "speed", "special_defense", "Velocidad+, Defensa Especial-"));
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
        //System.out.println("Obteniendo todas las naturalezas...");
        //System.out.println("Número de naturalezas: " + natures.size());
        for (Nature nature : natures.values()) {
            //System.out.println("Añadiendo naturaleza: " + nature.getId() + " - " + nature.getName());
            result.put(nature.getId(), nature.toMap());
        }
        //System.out.println("Total de naturalezas en resultado: " + result.size());
        return result;
    }
} 