package com.rpgen.web;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rpgen.core.battle.BattleSystem;
import com.rpgen.core.battle.BaseBattleSystem;
import com.rpgen.core.entity.Character;
import com.rpgen.core.entity.Entity;
import com.rpgen.core.action.BasicAttack;
import com.rpgen.core.action.PredefinedActions;
import com.google.gson.reflect.TypeToken;
import com.rpgen.core.battle.BattleListener;
import com.rpgen.core.action.GameAction;

import java.util.*;
import java.util.stream.Collectors;

public class BattleServer {
    private final Gson gson;
    private static BattleSystem battleSystem;
    private static Map<String, Character> characters = new HashMap<>();

    public BattleServer() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void init() {
        // Inicializar el sistema de batalla
        battleSystem = new BaseBattleSystem();
        
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
            String id = UUID.randomUUID().toString();
            Character character = new Character(
                id,
                (String) data.get("name"),
                ((Double) data.get("maxHealth")).intValue(),
                ((Double) data.get("attack")).intValue(),
                ((Double) data.get("defense")).intValue()
            );
            characters.put(id, character);
            return gson.toJson(character);
        });

        post("/api/battle/start", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, Object> data = gson.fromJson(req.body(), new TypeToken<Map<String, Object>>(){}.getType());
                List<String> team1Ids = gson.fromJson(gson.toJson(data.get("team1")), new TypeToken<List<String>>(){}.getType());
                List<String> team2Ids = gson.fromJson(gson.toJson(data.get("team2")), new TypeToken<List<String>>(){}.getType());

                if (team1Ids.isEmpty() || team2Ids.isEmpty()) {
                    return gson.toJson(Map.of(
                        "status", "error",
                        "message", "Ambos equipos deben tener al menos un personaje"
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

                if (team1.size() != team1Ids.size() || team2.size() != team2Ids.size()) {
                    return gson.toJson(Map.of(
                        "status", "error",
                        "message", "Uno o más personajes no fueron encontrados"
                    ));
                }

                battleSystem.initialize(team1, team2);
                return gson.toJson(Map.of("status", "success"));
            } catch (Exception e) {
                return gson.toJson(Map.of(
                    "status", "error",
                    "message", "Error al iniciar la batalla: " + e.getMessage()
                ));
            }
        });

        post("/api/battle/action", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, String> params = gson.fromJson(req.body(), new TypeToken<Map<String, String>>(){}.getType());
                
                String sourceId = params.get("sourceId");
                String targetId = params.get("targetId");
                String actionType = params.get("actionType");
                
                if (sourceId == null || targetId == null || actionType == null) {
                    return gson.toJson(Map.of(
                        "status", "error",
                        "message", "Faltan parámetros requeridos"
                    ));
                }
            
                Character source = characters.get(sourceId);
                Character target = characters.get(targetId);
            
                if (source == null || target == null) {
                    return gson.toJson(Map.of(
                        "status", "error",
                        "message", "Personajes no encontrados"
                    ));
                }
                
                if (!source.isAlive() || !target.isAlive()) {
                    return gson.toJson(Map.of(
                        "status", "error",
                        "message", "Uno o ambos personajes no están vivos"
                    ));
                }
                
                GameAction action;
                String actionId = UUID.randomUUID().toString();
                
                switch (actionType.toLowerCase()) {
                    case "rock":
                        action = PredefinedActions.createRockAction(actionId);
                        break;
                    case "paper":
                        action = PredefinedActions.createPaperAction(actionId);
                        break;
                    case "scissors":
                        action = PredefinedActions.createScissorsAction(actionId);
                        break;
                    default:
                        return gson.toJson(Map.of(
                            "status", "error",
                            "message", "Tipo de acción no válido"
                        ));
                }
                
                battleSystem.addAction(source, target, action);
                
                return gson.toJson(Map.of(
                    "status", "success",
                    "actionType", actionType,
                    "sourceName", source.getName(),
                    "targetName", target.getName()
                ));
            } catch (Exception e) {
                return gson.toJson(Map.of(
                    "status", "error",
                    "message", "Error al procesar la acción: " + e.getMessage()
                ));
            }
        });

        post("/api/battle/process", (req, res) -> {
            res.type("application/json");
            battleSystem.processTurn();
            List<Entity> activeEntities = battleSystem.getActiveEntities();
            List<String> actions = new ArrayList<>();
            
            BattleListener listener = new BattleListener() {
                @Override
                public void onBattleStart() {
                    // No es necesario implementar
                }

                @Override
                public void onTurnStart() {
                    // No es necesario implementar
                }

                @Override
                public void onActionExecuted(Entity source, Entity target, GameAction action) {
                    actions.add(String.format("%s usa %s contra %s", 
                        source.getName(), action.getName(), target.getName()));
                }

                @Override
                public void onEntityDamaged(Entity entity, int damage) {
                    actions.add(String.format("%s recibe %d de daño", 
                        entity.getName(), damage));
                }

                @Override
                public void onEntityHealed(Entity entity, int amount) {
                    // No es necesario implementar
                }

                @Override
                public void onEntityDefeated(Entity entity) {
                    actions.add(String.format("%s ha sido derrotado", 
                        entity.getName()));
                }

                @Override
                public void onBattleEnd() {
                    // No es necesario implementar
                }
            };
            
            battleSystem.registerBattleListener(listener);
            
            return gson.toJson(Map.of(
                "status", "success",
                "isBattleOver", battleSystem.isBattleOver(),
                "actions", actions,
                "activeEntities", activeEntities.stream()
                    .map(e -> Map.of(
                        "id", e.getId(),
                        "name", e.getName(),
                        "health", e.getHealth(),
                        "maxHealth", e.getMaxHealth()
                    ))
                    .collect(Collectors.toList())
            ));
        });
    }
} 