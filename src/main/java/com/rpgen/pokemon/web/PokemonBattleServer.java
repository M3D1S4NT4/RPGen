package com.rpgen.pokemon.web;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.rpgen.pokemon.battle.PokemonBattleEngine;

public class PokemonBattleServer {
    private final Gson gson;
    private final Map<String, PokemonBattleEngine> activeBattles;

    public PokemonBattleServer() {
        this.gson = new Gson();
        this.activeBattles = new HashMap<>();
    }

    public void init() {
        // Configurar CORS
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type");
            response.type("application/json");
        });

        // Endpoint para iniciar una batalla
        post("/api/pokemon-battle/start", (req, res) -> {
            try {
                System.out.println("Recibida solicitud para iniciar batalla");
                //System.out.println("Body de la solicitud: " + req.body());
                
                Map<String, Object> data = gson.fromJson(req.body(), new TypeToken<Map<String, Object>>(){}.getType());
                List<Map<String, Object>> team1 = gson.fromJson(
                    gson.toJson(data.get("team1")),
                    new TypeToken<List<Map<String, Object>>>(){}.getType()
                );
                List<Map<String, Object>> team2 = gson.fromJson(
                    gson.toJson(data.get("team2")),
                    new TypeToken<List<Map<String, Object>>>(){}.getType()
                );

                if (team1 == null || team2 == null) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "error", "Se requieren ambos equipos"
                    ));
                }

                String battleId = UUID.randomUUID().toString();
                PokemonBattleEngine battle = new PokemonBattleEngine();
                battle.initialize(team1, team2);
                activeBattles.put(battleId, battle);

                System.out.println("Batalla iniciada con ID: " + battleId);
                //System.out.println("Equipo 1: " + team1.size() + " Pokémon");
                //System.out.println("Equipo 2: " + team2.size() + " Pokémon");
                
                return gson.toJson(Map.of(
                    "status", "success",
                    "battleId", battleId,
                    "message", "Batalla iniciada correctamente"
                ));
            } catch (Exception e) {
                System.err.println("Error al iniciar la batalla: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Error al iniciar la batalla: " + e.getMessage()
                ));
            }
        });

        // Endpoint para realizar una acción en la batalla
        post("/api/pokemon-battle/:battleId/action", (req, res) -> {
            try {
                String battleId = req.params(":battleId");
                PokemonBattleEngine battle = activeBattles.get(battleId);

                if (battle == null) {
                    res.status(404);
                    return gson.toJson(Map.of(
                        "error", "Batalla no encontrada"
                    ));
                }

                Map<String, Object> data = gson.fromJson(req.body(), new TypeToken<Map<String, Object>>(){}.getType());
                Map<String, Object> source = gson.fromJson(
                    gson.toJson(data.get("source")),
                    new TypeToken<Map<String, Object>>(){}.getType()
                );
                Map<String, Object> target = gson.fromJson(
                    gson.toJson(data.get("target")),
                    new TypeToken<Map<String, Object>>(){}.getType()
                );
                Map<String, Object> action = gson.fromJson(
                    gson.toJson(data.get("action")),
                    new TypeToken<Map<String, Object>>(){}.getType()
                );

                battle.addAction(source, target, action);
                res.type("application/json");
                return gson.toJson(Map.of(
                    "message", "Acción añadida correctamente"
                ));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Error al procesar la acción: " + e.getMessage()
                ));
            }
        });

        // Endpoint para cambiar de Pokémon
        post("/api/pokemon-battle/:battleId/switch", (req, res) -> {
            try {
                String battleId = req.params(":battleId");
                PokemonBattleEngine battle = activeBattles.get(battleId);

                if (battle == null) {
                    res.status(404);
                    return gson.toJson(Map.of(
                        "error", "Batalla no encontrada"
                    ));
                }

                Map<String, Object> data = gson.fromJson(req.body(), new TypeToken<Map<String, Object>>(){}.getType());
                Map<String, Object> newPokemon = gson.fromJson(
                    gson.toJson(data.get("newPokemon")),
                    new TypeToken<Map<String, Object>>(){}.getType()
                );
                boolean isTeam1 = (boolean) data.get("isTeam1");

                Map<String, Object> result = battle.switchPokemon(newPokemon, isTeam1);
                res.type("application/json");
                return gson.toJson(result);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Error al cambiar Pokémon: " + e.getMessage()
                ));
            }
        });

        // Endpoint para procesar un turno
        post("/api/pokemon-battle/:battleId/process-turn", (req, res) -> {
            try {
                String battleId = req.params(":battleId");
                PokemonBattleEngine battle = activeBattles.get(battleId);

                if (battle == null) {
                    res.status(404);
                    return gson.toJson(Map.of(
                        "error", "Batalla no encontrada"
                    ));
                }

                battle.processTurn();
                res.type("application/json");
                return gson.toJson(Map.of(
                    "message", "Turno procesado"
                ));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Error al procesar el turno: " + e.getMessage()
                ));
            }
        });

        post("/api/pokemon-battle/type-effectiveness", (req, res) -> {
            try {
                // Parsear el JSON de la solicitud
                JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
                
                // Obtener los tipos del ataque y del defensor
                String attackType = json.get("attackType").getAsString();
                JsonArray defenderTypesArray = json.getAsJsonArray("defenderTypes");
                List<String> defenderTypes = new ArrayList<>();
                for (JsonElement type : defenderTypesArray) {
                    defenderTypes.add(type.getAsString());
                }

                // Validar que los tipos sean válidos
                if (attackType == null || defenderTypes.isEmpty()) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "error", "Tipos de Pokémon no válidos"
                    ));
                }

                // Crear una instancia de PokemonBattleEngine para calcular la efectividad
                PokemonBattleEngine battle = new PokemonBattleEngine();
                double effectiveness = battle.calculateTypeEffectiveness(attackType, defenderTypes);

                // Devolver la efectividad como JSON
                JsonObject response = new JsonObject();
                response.addProperty("effectiveness", effectiveness);
                return response.toString();

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return gson.toJson(Map.of(
                    "error", "Error al calcular la efectividad de tipos: " + e.getMessage()
                ));
            }
        });
    }
} 