package com.rpgen.web;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rpgen.core.battle.BattleSystem;
import com.rpgen.core.battle.BaseBattleSystem;
import com.rpgen.core.entity.Entity;
import com.rpgen.core.entity.Character;
import com.rpgen.core.action.BasicAttack;
import com.rpgen.core.action.PredefinedActions;
import com.rpgen.core.action.CombatCommand;
import com.rpgen.core.action.CombatCommandFactory;
import com.google.gson.reflect.TypeToken;
import com.rpgen.core.battle.BattleListener;
import com.rpgen.core.action.GameAction;

import java.util.*;
import java.util.stream.Collectors;

public class BattleServer {
    private final Gson gson;
    private final BattleSystem battleSystem;
    private final Map<String, List<Entity>> teams;
    private final Map<String, Character> characters;

    public BattleServer() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.battleSystem = new BaseBattleSystem();
        this.teams = new HashMap<>();
        this.characters = new HashMap<>();
    }

    public void init() {
        // Inicializar el sistema de batalla
        // battleSystem = new BaseBattleSystem();
        
        // Configurar el servidor
        // port(4567); // ELIMINADA: solo debe estar en Main.java
        //staticFileLocation("src/main/resources/public");

        // Rutas de la API
        get("/api/characters", (req, res) -> {
            res.type("application/json");
            return gson.toJson(characters.values());
        });

        post("/api/characters", (req, res) -> {
            res.type("application/json");
            Map<String, Object> data = gson.fromJson(req.body(), Map.class);
            Character character = new Character(
                (String) data.get("name"),
                ((Double) data.get("maxHealth")).intValue(),
                ((Double) data.get("attack")).intValue(),
                ((Double) data.get("defense")).intValue()
            );
            characters.put(character.getId(), character);
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

            Map<String, Object> data = gson.fromJson(req.body(), Map.class);
            
            // Crear un nuevo personaje con los datos actualizados
            Character updatedCharacter = new Character(
                (String) data.get("name"),
                ((Double) data.get("maxHealth")).intValue(),
                ((Double) data.get("attack")).intValue(),
                ((Double) data.get("defense")).intValue()
            );
            
            characters.put(id, updatedCharacter);
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
                Map<String, Object> data = gson.fromJson(req.body(), Map.class);
                List<String> team1Ids = (List<String>) data.get("team1");
                List<String> team2Ids = (List<String>) data.get("team2");

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
                Map<String, Object> data = gson.fromJson(req.body(), Map.class);
                String sourceId = (String) data.get("sourceId");
                String targetId = (String) data.get("targetId");
                String actionType = (String) data.get("actionType");
                
                if (sourceId == null || targetId == null || actionType == null) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "error", "Se requieren sourceId, targetId y actionType"
                    ));
                }
                
                Character source = characters.get(sourceId);
                Character target = characters.get(targetId);
                
                if (source == null || target == null) {
                    res.status(404);
                    return gson.toJson(Map.of(
                        "error", "Personaje no encontrado"
                    ));
                }
                
                try {
                    addAction(UUID.randomUUID().toString(), source, target, actionType);
                    
                    return gson.toJson(Map.of(
                        "status", "success"
                    ));
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "error", "Error al ejecutar la acción: " + e.getMessage()
                    ));
                }
            } catch (Exception e) {
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
    }

    public void initializeBattle(String battleId, List<Entity> team1, List<Entity> team2) {
        teams.put(battleId + "_team1", new ArrayList<>(team1));
        teams.put(battleId + "_team2", new ArrayList<>(team2));
        battleSystem.initialize(team1, team2);
    }

    public void addAction(String battleId, Entity source, Entity target, String actionName) {
        CombatCommand command = CombatCommandFactory.createCommand(source, target, actionName);
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