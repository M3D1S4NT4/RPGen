package com.rpgen.web;

import com.rpgen.pokemon.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import spark.Spark;
import java.util.*;

public class PokemonConfigServer {
    private final Gson gson = new Gson();

    public void init() {
        // Configurar CORS
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type");
        });

        // Obtener todas las naturalezas
        Spark.get("/api/pokemon/natures", (request, response) -> {
            response.type("application/json");
            return gson.toJson(NatureDatabase.getAllNaturesAsMap());
        });

        // Obtener todos los objetos
        Spark.get("/api/pokemon/items", (request, response) -> {
            response.type("application/json");
            return gson.toJson(ItemDatabase.getAllItemsAsMap());
        });

        // Obtener habilidades de un Pokémon específico
        Spark.get("/api/pokemon/:id/abilities", (request, response) -> {
            response.type("application/json");
            String pokemonId = request.params(":id");
            Pokemon pokemon = PokemonDatabase.getPokemon(pokemonId);
            if (pokemon != null) {
                List<Map<String, Object>> abilities = pokemon.getAbilities().stream()
                    .map(Ability::toMap)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                return gson.toJson(abilities);
            }
            return gson.toJson(new ArrayList<>());
        });

        // Configurar Pokémon
        Spark.post("/api/pokemon/:id/configure", (request, response) -> {
            response.type("application/json");
            String pokemonId = request.params(":id");
            
            //System.out.println("Configurando Pokémon con ID: " + pokemonId);
            //System.out.println("Body de la solicitud: " + request.body());
            
            Pokemon pokemon = PokemonDatabase.getPokemon(pokemonId);
            
            if (pokemon == null) {
                System.out.println("Pokémon no encontrado con ID: " + pokemonId);
                response.status(404);
                return gson.toJson(Map.of("error", "Pokémon no encontrado con ID: " + pokemonId));
            }

            try {
                Map<String, Object> config = gson.fromJson(
                    request.body(),
                    new TypeToken<Map<String, Object>>(){}.getType()
                );
                //System.out.println("Configuración recibida: " + config);
                
                // Configurar movimientos seleccionados
                if (config.containsKey("selectedMoveIndices")) {
                    List<Integer> selectedMoveIndices = new ArrayList<>();
                    List<?> indices = (List<?>) config.get("selectedMoveIndices");
                    for (Object index : indices) {
                        if (index instanceof Number) {
                            selectedMoveIndices.add(((Number) index).intValue());
                        }
                    }
                    pokemon.setSelectedMoveIndices(selectedMoveIndices);
                    //System.out.println("Movimientos configurados: " + selectedMoveIndices);
                }
                
                // Configurar habilidad
                if (config.containsKey("selectedAbilityId")) {
                    String abilityId = (String) config.get("selectedAbilityId");
                    if (abilityId != null && !abilityId.isEmpty()) {
                        pokemon.getAbilities().stream()
                            .filter(ability -> ability.getId().equals(abilityId))
                            .findFirst()
                            .ifPresent(pokemon::setSelectedAbility);
                        //System.out.println("Habilidad configurada: " + abilityId);
                    }
                }

                // Configurar objeto equipado
                if (config.containsKey("heldItemId")) {
                    String itemId = (String) config.get("heldItemId");
                    if (itemId != null && !itemId.isEmpty()) {
                        HeldItem item = ItemDatabase.getItem(itemId);
                        pokemon.setHeldItem(item);
                        //System.out.println("Objeto configurado: " + itemId);
                    } else {
                        pokemon.setHeldItem(null);
                        //System.out.println("Objeto removido");
                    }
                }

                // Configurar naturaleza
                if (config.containsKey("natureId")) {
                    String natureId = (String) config.get("natureId");
                    if (natureId != null && !natureId.isEmpty()) {
                        Nature nature = NatureDatabase.getNature(natureId);
                        if (nature != null) {
                            pokemon.setNature(nature);
                            //System.out.println("Naturaleza configurada: " + natureId);
                        }
                    }
                }

                // Configurar IVs
                if (config.containsKey("ivs")) {
                    Map<String, Object> ivs = gson.fromJson(
                        gson.toJson(config.get("ivs")),
                        new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType()
                    );
                    Stats currentStats = pokemon.getStats();
                    
                    int hpIV = ((Number) ivs.getOrDefault("hp", 31)).intValue();
                    int attackIV = ((Number) ivs.getOrDefault("attack", 31)).intValue();
                    int defenseIV = ((Number) ivs.getOrDefault("defense", 31)).intValue();
                    int specialAttackIV = ((Number) ivs.getOrDefault("specialAttack", 31)).intValue();
                    int specialDefenseIV = ((Number) ivs.getOrDefault("specialDefense", 31)).intValue();
                    int speedIV = ((Number) ivs.getOrDefault("speed", 31)).intValue();

                    Stats newStats = new Stats(
                        currentStats.getHp(), currentStats.getAttack(), currentStats.getDefense(),
                        currentStats.getSpecialAttack(), currentStats.getSpecialDefense(), currentStats.getSpeed(),
                        hpIV, attackIV, defenseIV, specialAttackIV, specialDefenseIV, speedIV,
                        currentStats.getHpEV(), currentStats.getAttackEV(), currentStats.getDefenseEV(),
                        currentStats.getSpecialAttackEV(), currentStats.getSpecialDefenseEV(), currentStats.getSpeedEV()
                    );
                    pokemon.setStats(newStats);
                    //System.out.println("IVs configurados");
                }

                // Configurar EVs
                if (config.containsKey("evs")) {
                    Map<String, Object> evs = gson.fromJson(
                        gson.toJson(config.get("evs")),
                        new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType()
                    );
                    Stats currentStats = pokemon.getStats();
                    
                    int hpEV = ((Number) evs.getOrDefault("hp", 0)).intValue();
                    int attackEV = ((Number) evs.getOrDefault("attack", 0)).intValue();
                    int defenseEV = ((Number) evs.getOrDefault("defense", 0)).intValue();
                    int specialAttackEV = ((Number) evs.getOrDefault("specialAttack", 0)).intValue();
                    int specialDefenseEV = ((Number) evs.getOrDefault("specialDefense", 0)).intValue();
                    int speedEV = ((Number) evs.getOrDefault("speed", 0)).intValue();

                    Stats newStats = new Stats(
                        currentStats.getHp(), currentStats.getAttack(), currentStats.getDefense(),
                        currentStats.getSpecialAttack(), currentStats.getSpecialDefense(), currentStats.getSpeed(),
                        currentStats.getHpIV(), currentStats.getAttackIV(), currentStats.getDefenseIV(),
                        currentStats.getSpecialAttackIV(), currentStats.getSpecialDefenseIV(), currentStats.getSpeedIV(),
                        hpEV, attackEV, defenseEV, specialAttackEV, specialDefenseEV, speedEV
                    );
                    
                    if (newStats.isValidEVs()) {
                        pokemon.setStats(newStats);
                        //System.out.println("EVs configurados");
                    } else {
                        response.status(400);
                        return gson.toJson(Map.of("error", "Los EVs totales no pueden exceder 510"));
                    }
                }

                // Configurar nivel
                if (config.containsKey("level")) {
                    int level = ((Number) config.get("level")).intValue();
                    pokemon.setLevel(level);
                    //System.out.println("Nivel configurado: " + level);
                }

                Map<String, Object> result = pokemon.toMap();
                //System.out.println("Configuración completada exitosamente");
                return gson.toJson(result);
            } catch (Exception e) {
                System.err.println("Error al configurar el Pokémon: " + e.getMessage());
                e.printStackTrace();
                response.status(500);
                return gson.toJson(Map.of("error", "Error al configurar el Pokémon: " + e.getMessage()));
            }
        });

        // Obtener estadísticas de un Pokémon
        Spark.get("/api/pokemon/:id/stats", (request, response) -> {
            response.type("application/json");
            String pokemonId = request.params(":id");
            Pokemon pokemon = PokemonDatabase.getPokemon(pokemonId);
            
            if (pokemon != null) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("baseStats", pokemon.getStats().toMap());
                stats.put("finalStats", Map.of(
                    "hp", pokemon.getMaxHealth(),
                    "attack", pokemon.getAttack(),
                    "defense", pokemon.getDefense(),
                    "speed", pokemon.getSpeed(),
                    "specialAttack", pokemon.getSpecialAttack(),
                    "specialDefense", pokemon.getSpecialDefense()
                ));
                stats.put("nature", pokemon.getNature() != null ? pokemon.getNature().toMap() : null);
                stats.put("heldItem", pokemon.getHeldItem() != null ? pokemon.getHeldItem().toMap() : null);
                stats.put("level", pokemon.getLevel());
                return gson.toJson(stats);
            }
            
            response.status(404);
            return gson.toJson(Map.of("error", "Pokémon no encontrado"));
        });
    }
} 