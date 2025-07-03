package com.rpgen.core.web;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rpgen.core.battle.BattleEngine;
import com.rpgen.core.entity.Entity;
import com.rpgen.core.entity.Character;
import com.rpgen.core.action.CombatCommand;
import com.rpgen.core.action.CombatCommandFactory;
import com.rpgen.core.action.GameAction;
import com.google.gson.reflect.TypeToken;

import java.util.*;
import java.util.stream.Collectors;
import java.io.*;

public class BattleServer {
    private final Gson gson;
    private final BattleEngine<Entity, GameAction> battleSystem;
    private final Map<String, List<Entity>> teams;
    private final Map<String, Character> characters;
    private final String CHARACTERS_FILE = "characters.json";

    public BattleServer() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.battleSystem = new com.rpgen.core.battle.DefaultBattleEngine();
        this.teams = new HashMap<>();
        this.characters = new HashMap<>();
        loadCharacters();
    }

    private void loadCharacters() {
        File file = new File(CHARACTERS_FILE);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Character[] loaded = gson.fromJson(reader, Character[].class);
                for (Character c : loaded) {
                    characters.put(c.getId(), c);
                }
            } catch (Exception e) {
                System.err.println("Error cargando personajes: " + e.getMessage());
            }
        } else {
            // Personajes por defecto
            Character c1 = new Character("Guerrero", 100, 20, 10);
            Character c2 = new Character("Mago", 70, 30, 5);
            Character c3 = new Character("Arquero", 80, 18, 8);
            characters.put(c1.getId(), c1);
            characters.put(c2.getId(), c2);
            characters.put(c3.getId(), c3);
            saveCharacters();
        }
    }

    private void saveCharacters() {
        try (Writer writer = new FileWriter(CHARACTERS_FILE)) {
            gson.toJson(characters.values(), writer);
        } catch (Exception e) {
            System.err.println("Error guardando personajes: " + e.getMessage());
        }
    }

    public void init() {
        // Rutas de la API
        get("/api/characters", (req, res) -> {
            res.type("application/json");
            return gson.toJson(characters.values());
        });

        post("/api/characters", (req, res) -> {
            res.type("application/json");
            Map<String, Object> data = gson.fromJson(req.body(), new TypeToken<Map<String, Object>>(){}.getType());
            Character character = new Character(
                (String) data.get("name"),
                ((Double) data.get("maxHealth")).intValue(),
                ((Double) data.get("attack")).intValue(),
                ((Double) data.get("defense")).intValue()
            );
            characters.put(character.getId(), character);
            saveCharacters();
            return gson.toJson(character);
        });

        put("/api/characters/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            Character character = characters.get(id);
            
            if (character == null) {
                res.status(404);
                return gson.toJson(Map.of(
                    "error", "Personaje no encontrado"
                ));
            }

            Map<String, Object> data = gson.fromJson(req.body(), new TypeToken<Map<String, Object>>(){}.getType());
            
            // Crear un nuevo personaje con los datos actualizados
            Character updatedCharacter = new Character(
                (String) data.get("name"),
                ((Double) data.get("maxHealth")).intValue(),
                ((Double) data.get("attack")).intValue(),
                ((Double) data.get("defense")).intValue()
            );
            
            characters.put(id, updatedCharacter);
            saveCharacters();
            return gson.toJson(updatedCharacter);
        });

        post("/api/battle/reset", (req, res) -> {
            res.type("application/json");
            try {
                // Resetear la salud de todos los personajes
                characters.values().forEach(character -> character.heal(character.getMaxHealth()));
                
                // Resetear el sistema de batalla
                battleSystem.initialize(new ArrayList<>(), new ArrayList<>());
                
                return gson.toJson(Map.of("status", "success"));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Error al resetear la batalla: " + e.getMessage()
                ));
            }
        });

        post("/api/battle/start", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, Object> data = gson.fromJson(req.body(), new TypeToken<Map<String, Object>>(){}.getType());
                List<?> team1Raw = (List<?>) data.get("team1");
                List<String> team1Ids = team1Raw.stream().map(Object::toString).collect(Collectors.toList());
                List<?> team2Raw = (List<?>) data.get("team2");
                List<String> team2Ids = team2Raw.stream().map(Object::toString).collect(Collectors.toList());

                if (team1Ids == null || team2Ids == null) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "error", "Se requieren ambos equipos"
                    ));
                }

                List<Entity> team1 = team1Ids.stream()
                    .map(id -> characters.get(id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                    
                List<Entity> team2 = team2Ids.stream()
                    .map(id -> characters.get(id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                if (team1.isEmpty() || team2.isEmpty()) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "error", "Los equipos no pueden estar vacíos"
                    ));
                }

                String battleId = UUID.randomUUID().toString();
                initializeBattle(battleId, team1, team2);
                return gson.toJson(Map.of(
                    "status", "success",
                    "battleId", battleId
                ));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Error al iniciar la batalla: " + e.getMessage()
                ));
            }
        });

        post("/api/battle/action", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, Object> data = gson.fromJson(req.body(), new TypeToken<Map<String, Object>>(){}.getType());
                String sourceId = (String) data.get("sourceId");
                String targetId = (String) data.get("targetId");
                String actionType = (String) data.get("actionType");
                //System.out.println("[ACTION] sourceId=" + sourceId + ", targetId=" + targetId + ", actionType=" + actionType);
                if (sourceId == null || targetId == null || actionType == null) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "error", "Se requieren sourceId, targetId y actionType"
                    ));
                }
                Character source = characters.get(sourceId);
                Character target = characters.get(targetId);
                if (source == null || target == null) {
                    System.err.println("[ERROR] Personaje no encontrado. source=" + source + ", target=" + target);
                    res.status(404);
                    return gson.toJson(Map.of(
                        "error", "Personaje no encontrado. sourceId=" + sourceId + ", targetId=" + targetId
                    ));
                }
                try {
                    addAction(UUID.randomUUID().toString(), source, target, actionType);
                    return gson.toJson(Map.of(
                        "status", "success"
                    ));
                } catch (Exception e) {
                    System.err.println("[ERROR] Error al ejecutar la acción: " + e.getMessage());
                    res.status(400);
                    return gson.toJson(Map.of(
                        "error", "Error al ejecutar la acción: " + e.getMessage()
                    ));
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Error al procesar la acción: " + e.getMessage());
                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Error al procesar la acción: " + e.getMessage()
                ));
            }
        });

        post("/api/battle/process", (req, res) -> {
            res.type("application/json");
            processTurn(UUID.randomUUID().toString());
            List<Entity> activeEntities = getActiveEntities(UUID.randomUUID().toString());
            List<String> actions = new ArrayList<>();
            
            return gson.toJson(Map.of(
                "status", "success",
                "isBattleOver", isBattleOver(UUID.randomUUID().toString()),
                "actions", actions,
                "activeEntities", activeEntities.stream()
                    .map(Entity::toString)
                    .collect(Collectors.toList())
            ));
        });

        // Endpoint para eliminar un personaje
        delete("/api/characters/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            Character removed = characters.remove(id);
            saveCharacters();
            if (removed == null) {
                res.status(404);
                return gson.toJson(Map.of("error", "Personaje no encontrado"));
            }
            return gson.toJson(Map.of("status", "success"));
        });
    }

    public void initializeBattle(String battleId, List<Entity> team1, List<Entity> team2) {
        teams.put(battleId + "_team1", new ArrayList<>(team1));
        teams.put(battleId + "_team2", new ArrayList<>(team2));
        battleSystem.initialize(team1, team2);
    }

    public void addAction(String battleId, Entity source, Entity target, String actionName) {
        // Asociar nombres de acción a GameAction
        GameAction action = null;
        switch (actionName) {
            case "basic":
                action = com.rpgen.core.action.PredefinedActions.createBasicAttack(source.getId());
                break;
            case "strong":
                action = com.rpgen.core.action.PredefinedActions.createStrongAttack(source.getId());
                break;
            case "shield":
                action = com.rpgen.core.action.PredefinedActions.createShield(source.getId());
                break;
            default:
                throw new IllegalArgumentException("Acción desconocida: " + actionName);
        }
        CombatCommand command = CombatCommandFactory.createCommand(source, target, actionName, action);
        battleSystem.addAction(source, target, command.getAction());
    }

    public void processTurn(String battleId) {
        battleSystem.processTurn();
    }

    public List<Entity> getActiveEntities(String battleId) {
        return battleSystem.getActiveEntities();
    }

    public boolean isBattleOver(String battleId) {
        return battleSystem.isBattleOver();
    }

    public List<Entity> getTeam(String battleId, int teamNumber) {
        return teams.get(battleId + "_team" + teamNumber);
    }
} 