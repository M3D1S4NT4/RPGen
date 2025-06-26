package com.rpgen.pokemon;

import java.util.*;

public class ItemDatabase {
    private static final Map<String, HeldItem> items = new HashMap<>();
    
    static {
        initializeItems();
    }
    
    private static void initializeItems() {
        // Objetos que aumentan estadísticas
        items.put("choice-band", new HeldItem("choice-band", "Banda Elegida", 
            "Aumenta el Ataque en 50% pero solo permite usar un movimiento", 
            "Aumenta el Ataque en 50%", 
            Map.of("attack", 1.5), "held"));
            
        items.put("choice-specs", new HeldItem("choice-specs", "Gafas Elegidas", 
            "Aumenta el Ataque Especial en 50% pero solo permite usar un movimiento", 
            "Aumenta el Ataque Especial en 50%", 
            Map.of("special_attack", 1.5), "held"));
            
        items.put("choice-scarf", new HeldItem("choice-scarf", "Pañuelo Elegido", 
            "Aumenta la Velocidad en 50% pero solo permite usar un movimiento", 
            "Aumenta la Velocidad en 50%", 
            Map.of("speed", 1.5), "held"));
            
        items.put("life-orb", new HeldItem("life-orb", "Orbe Vital", 
            "Aumenta el poder de los movimientos en 30% pero causa daño al usuario", 
            "Aumenta el poder de los movimientos en 30%", 
            Map.of("power", 1.3), "held"));
            
        items.put("expert-belt", new HeldItem("expert-belt", "Cinturón Experto", 
            "Aumenta el poder de movimientos super efectivos en 20%", 
            "Aumenta el poder de movimientos super efectivos", 
            Map.of("super_effective_power", 1.2), "held"));
            
        items.put("muscle-band", new HeldItem("muscle-band", "Banda Muscular", 
            "Aumenta el poder de movimientos físicos en 10%", 
            "Aumenta el poder de movimientos físicos", 
            Map.of("physical_power", 1.1), "held"));
            
        items.put("wise-glasses", new HeldItem("wise-glasses", "Gafas Sabias", 
            "Aumenta el poder de movimientos especiales en 10%", 
            "Aumenta el poder de movimientos especiales", 
            Map.of("special_power", 1.1), "held"));
            
        items.put("leftovers", new HeldItem("leftovers", "Restos", 
            "Restaura 1/16 de HP máximo al final de cada turno", 
            "Restaura HP gradualmente", 
            Map.of("hp_recovery", 0.0625), "held"));
            
        items.put("black-sludge", new HeldItem("black-sludge", "Lodo Negro", 
            "Restaura HP a Pokémon de tipo Veneno, daña a otros tipos", 
            "Restaura HP a Pokémon Veneno", 
            Map.of("poison_hp_recovery", 0.0625), "held"));
            
        items.put("rocky-helmet", new HeldItem("rocky-helmet", "Casco Pétreo", 
            "Daña al atacante cuando recibe un ataque físico", 
            "Daña al atacante", 
            Map.of("counter_damage", 0.25), "held"));
            
        items.put("assault-vest", new HeldItem("assault-vest", "Chaleco Asalto", 
            "Aumenta la Defensa Especial en 50% pero solo permite movimientos de ataque", 
            "Aumenta la Defensa Especial en 50%", 
            Map.of("special_defense", 1.5), "held"));
            
        items.put("eviolite", new HeldItem("eviolite", "Piedra Evo", 
            "Aumenta Defensa y Defensa Especial en 50% si el Pokémon puede evolucionar", 
            "Aumenta defensas si puede evolucionar", 
            Map.of("defense", 1.5, "special_defense", 1.5), "held"));
            
        items.put("focus-sash", new HeldItem("focus-sash", "Banda Focus", 
            "Permite sobrevivir un ataque que causaría debilitamiento con 1 HP", 
            "Sobrevive un ataque fatal", 
            Map.of("survive_fatal", 1.0), "held"));
            
        items.put("quick-powder", new HeldItem("quick-powder", "Polvo Rápido", 
            "Duplica la Velocidad de Ditto", 
            "Duplica la Velocidad de Ditto", 
            Map.of("speed", 2.0), "held"));
            
        items.put("light-ball", new HeldItem("light-ball", "Bola Luz", 
            "Duplica el Ataque y Ataque Especial de Pikachu", 
            "Duplica Ataque y Ataque Especial de Pikachu", 
            Map.of("attack", 2.0, "special_attack", 2.0), "held"));
            
        items.put("thick-club", new HeldItem("thick-club", "Hueso Grueso", 
            "Duplica el Ataque de Cubone y Marowak", 
            "Duplica el Ataque de Cubone/Marowak", 
            Map.of("attack", 2.0), "held"));
            
        items.put("deep-sea-scale", new HeldItem("deep-sea-scale", "Escama Marina", 
            "Duplica la Defensa Especial de Clamperl", 
            "Duplica la Defensa Especial de Clamperl", 
            Map.of("special_defense", 2.0), "held"));
            
        items.put("deep-sea-tooth", new HeldItem("deep-sea-tooth", "Diente Marino", 
            "Duplica el Ataque Especial de Clamperl", 
            "Duplica el Ataque Especial de Clamperl", 
            Map.of("special_attack", 2.0), "held"));
            
        items.put("metal-powder", new HeldItem("metal-powder", "Polvo Metal", 
            "Aumenta la Defensa de Ditto en 50%", 
            "Aumenta la Defensa de Ditto", 
            Map.of("defense", 1.5), "held"));
            
        items.put("soul-dew", new HeldItem("soul-dew", "Rocío Alma", 
            "Aumenta Ataque Especial y Defensa Especial de Latios/Latias en 50%", 
            "Aumenta Ataque Especial y Defensa Especial de Latios/Latias", 
            Map.of("special_attack", 1.5, "special_defense", 1.5), "held"));
            
        items.put("adamant-orb", new HeldItem("adamant-orb", "Orbe Adamante", 
            "Aumenta Ataque Especial y Defensa Especial de Dialga en 20%", 
            "Aumenta Ataque Especial y Defensa Especial de Dialga", 
            Map.of("special_attack", 1.2, "special_defense", 1.2), "held"));
            
        items.put("lustrous-orb", new HeldItem("lustrous-orb", "Orbe Lustroso", 
            "Aumenta Ataque Especial y Defensa Especial de Palkia en 20%", 
            "Aumenta Ataque Especial y Defensa Especial de Palkia", 
            Map.of("special_attack", 1.2, "special_defense", 1.2), "held"));
            
        items.put("griseous-orb", new HeldItem("griseous-orb", "Orbe Gris", 
            "Aumenta Ataque Especial y Defensa Especial de Giratina en 20%", 
            "Aumenta Ataque Especial y Defensa Especial de Giratina", 
            Map.of("special_attack", 1.2, "special_defense", 1.2), "held"));
    }
    
    public static HeldItem getItem(String id) {
        return items.get(id.toLowerCase());
    }
    
    public static List<HeldItem> getAllItems() {
        return new ArrayList<>(items.values());
    }
    
    public static HeldItem getRandomItem() {
        List<HeldItem> itemList = new ArrayList<>(items.values());
        return itemList.get(new Random().nextInt(itemList.size()));
    }
    
    public static Map<String, Object> getAllItemsAsMap() {
        Map<String, Object> result = new HashMap<>();
        System.out.println("Obteniendo todos los objetos...");
        System.out.println("Número de objetos: " + items.size());
        for (HeldItem item : items.values()) {
            System.out.println("Añadiendo objeto: " + item.getId() + " - " + item.getName());
            result.put(item.getId(), item.toMap());
        }
        System.out.println("Total de objetos en resultado: " + result.size());
        return result;
    }
    
    public static List<HeldItem> getItemsByCategory(String category) {
        return items.values().stream()
            .filter(item -> item.getCategory().equals(category))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
} 