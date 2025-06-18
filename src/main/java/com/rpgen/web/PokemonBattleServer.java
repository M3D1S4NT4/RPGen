package com.rpgen.web;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rpgen.pokemon.Pokemon;
import java.util.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class PokemonBattleServer {
    private final Gson gson;
    private final Map<String, PokemonBattle> activeBattles;

    public PokemonBattleServer() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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
                System.out.println("Body de la solicitud: " + req.body());
                
                Map<String, Object> data = gson.fromJson(req.body(), Map.class);
                List<Map<String, Object>> team1 = (List<Map<String, Object>>) data.get("team1");
                List<Map<String, Object>> team2 = (List<Map<String, Object>>) data.get("team2");

                if (team1 == null || team2 == null) {
                    res.status(400);
                    return gson.toJson(Map.of(
                        "error", "Se requieren ambos equipos"
                    ));
                }

                String battleId = UUID.randomUUID().toString();
                PokemonBattle battle = new PokemonBattle(team1, team2);
                activeBattles.put(battleId, battle);

                System.out.println("Batalla iniciada con ID: " + battleId);
                System.out.println("Equipo 1: " + team1.size() + " Pokémon");
                System.out.println("Equipo 2: " + team2.size() + " Pokémon");
                
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
                PokemonBattle battle = activeBattles.get(battleId);

                if (battle == null) {
                    res.status(404);
                    return gson.toJson(Map.of(
                        "error", "Batalla no encontrada"
                    ));
                }

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);
                Map<String, Object> source = (Map<String, Object>) data.get("source");
                Map<String, Object> target = (Map<String, Object>) data.get("target");
                Map<String, Object> action = (Map<String, Object>) data.get("action");

                Map<String, Object> result = battle.addAction(source, target, action);

                return gson.toJson(result);
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
                PokemonBattle battle = activeBattles.get(battleId);

                if (battle == null) {
                    res.status(404);
                    return gson.toJson(Map.of(
                        "error", "Batalla no encontrada"
                    ));
                }

                Map<String, Object> data = gson.fromJson(req.body(), Map.class);
                Map<String, Object> newPokemon = (Map<String, Object>) data.get("newPokemon");
                boolean isTeam1 = (boolean) data.get("isTeam1");

                Map<String, Object> result = battle.switchPokemon(newPokemon, isTeam1);

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
                PokemonBattle battle = activeBattles.get(battleId);

                if (battle == null) {
                    res.status(404);
                    return gson.toJson(Map.of(
                        "error", "Batalla no encontrada"
                    ));
                }

                Map<String, Object> response = battle.processTurn();

                return gson.toJson(response);
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

                // Crear una instancia de PokemonBattle para calcular la efectividad
                PokemonBattle battle = new PokemonBattle(new ArrayList<>(), new ArrayList<>());
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

    private static class PokemonBattle {
        private List<Map<String, Object>> team1;
        private List<Map<String, Object>> team2;
        private List<Map<String, Object>> pendingActions;
        private Random random;
        private boolean team1ActionSelected;
        private boolean team2ActionSelected;
        private boolean battleOver;
        private Map<String, Object> team1ActivePokemon;
        private Map<String, Object> team2ActivePokemon;

        public PokemonBattle(List<Map<String, Object>> team1, List<Map<String, Object>> team2) {
            this.team1 = new ArrayList<>(team1);
            this.team2 = new ArrayList<>(team2);
            this.pendingActions = new ArrayList<>();
            this.random = new Random();
            this.team1ActionSelected = false;
            this.team2ActionSelected = false;
            this.battleOver = false;
        }

        private boolean isTeamDefeated(List<Map<String, Object>> team) {
            return team.stream().allMatch(pokemon -> 
                ((Number) pokemon.getOrDefault("health", 0)).intValue() <= 0);
        }

        private void checkBattleEnd() {
            boolean team1Defeated = isTeamDefeated(team1);
            boolean team2Defeated = isTeamDefeated(team2);
            
            if (team1Defeated || team2Defeated) {
                battleOver = true;
                System.out.println("¡La batalla ha terminado!");
                if (team1Defeated && team2Defeated) {
                    System.out.println("¡Empate!");
                } else if (team1Defeated) {
                    System.out.println("¡El Equipo 2 ha ganado!");
                } else {
                    System.out.println("¡El Equipo 1 ha ganado!");
                }
            }
        }

        public Map<String, Object> addAction(Map<String, Object> source, Map<String, Object> target, Map<String, Object> action) {
            System.out.println("Servidor: addAction para instancia de PokemonBattle (hashCode): " + System.identityHashCode(this));
            if (battleOver) {
                System.out.println("No se pueden realizar más acciones, la batalla ha terminado");
                return Map.of("error", "La batalla ha terminado");
            }

            // Verificar si el Pokémon atacante está vivo
            Map<String, Object> sourceInTeam = findPokemonInTeams(source);
            if (sourceInTeam == null || ((Number) sourceInTeam.getOrDefault("health", 0)).intValue() <= 0) {
                System.out.println("El Pokémon atacante está debilitado y no puede atacar");
                return Map.of("error", "El Pokémon atacante está debilitado y no puede atacar");
            }

            pendingActions.add(Map.of(
                "source", source,
                "target", target,
                "action", action
            ));
            
            // Marcar que el equipo correspondiente ha seleccionado una acción
            String sourceName = (String) source.get("name");
            if (team1.stream().anyMatch(p -> p.get("id").equals(source.get("id")))) {
                team1ActionSelected = true;
                System.out.println("Servidor: addAction - Equipo 1 acción seleccionada para " + sourceName + " (ahora team1ActionSelected=true).");
            } else {
                team2ActionSelected = true;
                System.out.println("Servidor: addAction - Equipo 2 acción seleccionada para " + sourceName + " (ahora team2ActionSelected=true).");
            }

            return Map.of("message", "Acción añadida correctamente",
                          "team1ActionSelected", team1ActionSelected,
                          "team2ActionSelected", team2ActionSelected);
        }

        public Map<String, Object> switchPokemon(Map<String, Object> newPokemon, boolean isTeam1) {
            if (battleOver) {
                System.out.println("No se pueden realizar más acciones, la batalla ha terminado");
                return Map.of("error", "La batalla ha terminado");
            }

            List<Map<String, Object>> team = isTeam1 ? team1 : team2;
            for (int i = 0; i < team.size(); i++) {
                if (team.get(i).get("id").equals(newPokemon.get("id"))) {
                    // Verificar si el Pokémon está debilitado
                    if (((Number) team.get(i).getOrDefault("health", 0)).intValue() <= 0) {
                        return Map.of("error", "No puedes cambiar a un Pokémon debilitado");
                    }

                    // Actualizar el Pokémon en el equipo
                    team.set(i, newPokemon);
                    
                    // Actualizar el Pokémon activo
                    if (isTeam1) {
                        team1ActivePokemon = newPokemon;
                        team1ActionSelected = true;
                    } else {
                        team2ActivePokemon = newPokemon;
                        team2ActionSelected = true;
                    }

                    return Map.of(
                        "message", "Pokémon cambiado", 
                        "newActivePokemon", newPokemon,
                        "team1ActionSelected", team1ActionSelected,
                        "team2ActionSelected", team2ActionSelected
                    );
                }
            }
            return Map.of("error", "No se encontró el Pokémon en el equipo");
        }

        private Map<String, Object> findPokemonInTeams(Map<String, Object> pokemon) {
            // Buscar en el equipo 1
            for (Map<String, Object> p : team1) {
                if (p.get("id").equals(pokemon.get("id"))) {
                    return p;
                }
            }
            // Buscar en el equipo 2
            for (Map<String, Object> p : team2) {
                if (p.get("id").equals(pokemon.get("id"))) {
                    return p;
                }
            }
            return null;
        }

        private double calculateTypeEffectiveness(String attackType, List<String> defenderTypes) {
            double effectiveness = 1.0;
            
            // Tabla simplificada de efectividad de tipos
            Map<String, Map<String, Double>> typeChart = new HashMap<>();
            
            // Normal
            Map<String, Double> normal = new HashMap<>();
            normal.put("rock", 0.5); normal.put("ghost", 0.0); normal.put("steel", 0.5);
            typeChart.put("normal", normal);

            // Fire
            Map<String, Double> fire = new HashMap<>();
            fire.put("fire", 0.5); fire.put("water", 0.5); fire.put("grass", 2.0);
            fire.put("ice", 2.0); fire.put("bug", 2.0); fire.put("rock", 0.5);
            fire.put("dragon", 0.5); fire.put("steel", 2.0);
            typeChart.put("fire", fire);

            // Water
            Map<String, Double> water = new HashMap<>();
            water.put("fire", 2.0); water.put("water", 0.5); water.put("grass", 0.5);
            water.put("ground", 2.0); water.put("rock", 2.0); water.put("dragon", 0.5);
            typeChart.put("water", water);

            // Electric
            Map<String, Double> electric = new HashMap<>();
            electric.put("water", 2.0); electric.put("electric", 0.5); electric.put("grass", 0.5);
            electric.put("ground", 0.0); electric.put("flying", 2.0); electric.put("dragon", 0.5);
            typeChart.put("electric", electric);

            // Grass
            Map<String, Double> grass = new HashMap<>();
            grass.put("fire", 0.5); grass.put("water", 2.0); grass.put("grass", 0.5);
            grass.put("poison", 0.5); grass.put("ground", 2.0); grass.put("flying", 0.5);
            grass.put("bug", 0.5); grass.put("rock", 2.0); grass.put("dragon", 0.5);
            grass.put("steel", 0.5);
            typeChart.put("grass", grass);

            // Ice
            Map<String, Double> ice = new HashMap<>();
            ice.put("fire", 0.5); ice.put("water", 0.5); ice.put("grass", 2.0);
            ice.put("ice", 0.5); ice.put("ground", 2.0); ice.put("flying", 2.0);
            ice.put("dragon", 2.0); ice.put("steel", 0.5);
            typeChart.put("ice", ice);

            // Fighting
            Map<String, Double> fighting = new HashMap<>();
            fighting.put("normal", 2.0); fighting.put("ice", 2.0); fighting.put("poison", 0.5);
            fighting.put("flying", 0.5); fighting.put("psychic", 0.5); fighting.put("bug", 0.5);
            fighting.put("rock", 2.0); fighting.put("ghost", 0.0); fighting.put("dark", 2.0);
            fighting.put("steel", 2.0);
            typeChart.put("fighting", fighting);

            // Poison
            Map<String, Double> poison = new HashMap<>();
            poison.put("grass", 2.0); poison.put("poison", 0.5); poison.put("ground", 0.5);
            poison.put("rock", 0.5); poison.put("ghost", 0.5); poison.put("steel", 0.0);
            typeChart.put("poison", poison);

            // Ground
            Map<String, Double> ground = new HashMap<>();
            ground.put("fire", 2.0); ground.put("electric", 2.0); ground.put("grass", 0.5);
            ground.put("poison", 2.0); ground.put("flying", 0.0); ground.put("bug", 0.5);
            ground.put("rock", 2.0); ground.put("steel", 2.0);
            typeChart.put("ground", ground);

            // Flying
            Map<String, Double> flying = new HashMap<>();
            flying.put("electric", 0.5); flying.put("grass", 2.0); flying.put("fighting", 2.0);
            flying.put("bug", 2.0); flying.put("rock", 0.5); flying.put("steel", 0.5);
            typeChart.put("flying", flying);

            // Psychic
            Map<String, Double> psychic = new HashMap<>();
            psychic.put("fighting", 2.0); psychic.put("poison", 2.0); psychic.put("psychic", 0.5);
            psychic.put("dark", 0.0); psychic.put("steel", 0.5);
            typeChart.put("psychic", psychic);

            // Bug
            Map<String, Double> bug = new HashMap<>();
            bug.put("fire", 0.5); bug.put("grass", 2.0); bug.put("fighting", 0.5);
            bug.put("poison", 0.5); bug.put("flying", 0.5); bug.put("psychic", 2.0);
            bug.put("ghost", 0.5); bug.put("dark", 2.0); bug.put("steel", 0.5);
            typeChart.put("bug", bug);

            // Rock
            Map<String, Double> rock = new HashMap<>();
            rock.put("fire", 2.0); rock.put("ice", 2.0); rock.put("fighting", 0.5);
            rock.put("ground", 0.5); rock.put("flying", 2.0); rock.put("bug", 2.0);
            rock.put("steel", 0.5);
            typeChart.put("rock", rock);

            // Ghost
            Map<String, Double> ghost = new HashMap<>();
            ghost.put("normal", 0.0); ghost.put("psychic", 2.0); ghost.put("ghost", 2.0);
            ghost.put("dark", 0.5);
            typeChart.put("ghost", ghost);

            // Dragon
            Map<String, Double> dragon = new HashMap<>();
            dragon.put("dragon", 2.0); dragon.put("steel", 0.5);
            typeChart.put("dragon", dragon);

            // Dark
            Map<String, Double> dark = new HashMap<>();
            dark.put("fighting", 0.5); dark.put("psychic", 2.0); dark.put("ghost", 2.0);
            dark.put("dark", 0.5);
            typeChart.put("dark", dark);

            // Steel
            Map<String, Double> steel = new HashMap<>();
            steel.put("fire", 0.5); steel.put("water", 0.5); steel.put("electric", 0.5);
            steel.put("ice", 2.0); steel.put("rock", 2.0); steel.put("steel", 0.5);
            typeChart.put("steel", steel);

            // Fairy
            Map<String, Double> fairy = new HashMap<>();
            fairy.put("fire", 0.5); fairy.put("fighting", 2.0); fairy.put("poison", 0.5);
            fairy.put("dragon", 2.0); fairy.put("dark", 2.0); fairy.put("steel", 0.5);
            typeChart.put("fairy", fairy);
            
            for (String defenderType : defenderTypes) {
                Map<String, Double> typeRelations = typeChart.getOrDefault(attackType, Collections.emptyMap());
                effectiveness *= typeRelations.getOrDefault(defenderType, 1.0);
            }
            
            return effectiveness;
        }

        public Map<String, Object> processTurn() {
            System.out.println("Servidor: Procesando turno para instancia de PokemonBattle (hashCode): " + System.identityHashCode(this));
            Map<String, Object> response = new HashMap<>();
            try {
                if (battleOver) {
                    System.out.println("No se pueden realizar más acciones, la batalla ha terminado");
                    return Map.of(
                        "error", "La batalla ha terminado",
                        "battleOver", true,
                        "message", "La batalla ha terminado"
                    );
                }

                System.out.println("Servidor: Verificando flags de acción seleccionada. Equipo 1: " + team1ActionSelected + ", Equipo 2: " + team2ActionSelected);
                if (!team1ActionSelected || !team2ActionSelected) {
                    System.out.println("Servidor: !ENTRANDO EN BLOQUE DE ERROR! Equipo 1: " + team1ActionSelected + ", Equipo 2: " + team2ActionSelected);
                    return Map.of(
                        "error", "Ambos equipos deben seleccionar una acción antes de procesar el turno",
                        "battleOver", false
                    );
                }

                System.out.println("Procesando turno con " + pendingActions.size() + " acciones pendientes");
                
                // Ordenar las acciones por velocidad del Pokémon
                pendingActions.sort((a, b) -> {
                    Map<String, Object> sourceA = (Map<String, Object>) a.get("source");
                    Map<String, Object> sourceB = (Map<String, Object>) b.get("source");
                    int speedA = ((Number) sourceA.getOrDefault("speed", 0)).intValue();
                    int speedB = ((Number) sourceB.getOrDefault("speed", 0)).intValue();
                    return Integer.compare(speedB, speedA); // Orden descendente (más rápido primero)
                });

                // Lista para almacenar las acciones procesadas
                List<Map<String, Object>> processedActions = new ArrayList<>();
                
                // Procesar las acciones secuencialmente
                for (Map<String, Object> action : pendingActions) {
                    Map<String, Object> source = (Map<String, Object>) action.get("source");
                    Map<String, Object> target = (Map<String, Object>) action.get("target");
                    Map<String, Object> move = (Map<String, Object>) action.get("action");

                    if (source == null || target == null || move == null) {
                        System.err.println("Error: Datos de acción incompletos");
                        continue;
                    }

                    // Verificar si el Pokémon atacante sigue vivo
                    Map<String, Object> sourceInTeam = findPokemonInTeams(source);
                    if (sourceInTeam == null || ((Number) sourceInTeam.getOrDefault("health", 0)).intValue() <= 0) {
                        System.out.println("El Pokémon atacante " + sourceInTeam.get("name") + " está debilitado");
                        continue;
                    }

                    // Verificar si el Pokémon objetivo sigue vivo y obtener el Pokémon activo actual
                    Map<String, Object> targetInTeam = findPokemonInTeams(target);
                    if (targetInTeam == null) {
                        System.err.println("Error: No se encontró el Pokémon objetivo en los equipos");
                        continue;
                    }

                    // Obtener el equipo del objetivo
                    boolean isTeam1Target = team1.stream().anyMatch(p -> p.get("id").equals(target.get("id")));
                    Map<String, Object> currentActiveTarget = isTeam1Target ? team1ActivePokemon : team2ActivePokemon;

                    // Si el objetivo ha sido cambiado, usar el nuevo Pokémon activo
                    if (!currentActiveTarget.get("id").equals(targetInTeam.get("id"))) {
                        targetInTeam = currentActiveTarget;
                    }

                    int targetHealth = ((Number) targetInTeam.getOrDefault("health", 0)).intValue();
                    if (targetHealth <= 0) {
                        System.out.println("El Pokémon objetivo " + targetInTeam.get("name") + " ya está debilitado");
                        continue;
                    }

                    System.out.println("Procesando acción: " + source.get("name") + " -> " + targetInTeam.get("name") + " usando " + move.get("name"));

                    // Verificar precisión
                    int accuracy = ((Number) move.getOrDefault("accuracy", 100)).intValue();
                    if (random.nextInt(100) >= accuracy) {
                        System.out.println("El movimiento falló por precisión");
                        continue;
                    }

                    // Calcular daño base
                    int power = ((Number) move.getOrDefault("power", 40)).intValue();
                    String category = (String) move.getOrDefault("category", "physical");
                    
                    int attackStat = category.equals("special") ? 
                        ((Number) source.getOrDefault("specialAttack", 50)).intValue() : 
                        ((Number) source.getOrDefault("attack", 50)).intValue();
                    
                    int defenseStat = category.equals("special") ? 
                        ((Number) targetInTeam.getOrDefault("specialDefense", 50)).intValue() : 
                        ((Number) targetInTeam.getOrDefault("defense", 50)).intValue();

                    // Fórmula de daño simplificada
                    int baseDamage = (int) ((((2 * 50 / 5 + 2) * attackStat * power / defenseStat) / 50) + 2);

                    // Calcular efectividad del tipo
                    String attackType = (String) move.getOrDefault("type", "normal");
                    List<String> defenderTypes = (List<String>) targetInTeam.getOrDefault("types", List.of("normal"));
                    double typeEffectiveness = calculateTypeEffectiveness(attackType, defenderTypes);

                    // Aplicar modificadores
                    double damage = baseDamage * typeEffectiveness;
                    
                    // Aplicar variación aleatoria (85-100%)
                    damage *= (0.85 + (random.nextDouble() * 0.15));
                    
                    // Redondear el daño final
                    int finalDamage = (int) Math.round(damage);

                    // Aplicar el daño
                    int currentHealth = ((Number) targetInTeam.getOrDefault("health", 0)).intValue();
                    int maxHealth = ((Number) targetInTeam.getOrDefault("maxHealth", 0)).intValue();
                    targetInTeam.put("health", Math.max(0, currentHealth - finalDamage));

                    System.out.println(String.format(
                        "Ataque: %s -> %s, Daño: %d (Base: %d, Efectividad: %.2f)",
                        source.get("name"), targetInTeam.get("name"), finalDamage, baseDamage, typeEffectiveness
                    ));

                    // Añadir la acción procesada a la lista
                    processedActions.add(Map.of(
                        "source", source,
                        "target", targetInTeam,
                        "move", move,
                        "damage", finalDamage,
                        "effectiveness", typeEffectiveness
                    ));

                    // Verificar si el Pokémon objetivo ha sido debilitado
                    if (targetInTeam.get("health").equals(0)) {
                        System.out.println(targetInTeam.get("name") + " ha sido debilitado");
                        checkBattleEnd();
                        if (battleOver) {
                            return Map.of("battleOver", true, "message", "La batalla ha terminado");
                        }
                    }
                }

                // Añadir los equipos actualizados y las acciones procesadas a la respuesta
                response.put("team1", team1);
                response.put("team2", team2);
                response.put("actions", processedActions);
                response.put("battleOver", battleOver);
                response.put("message", battleOver ? "La batalla ha terminado" : "Turno procesado correctamente");
                
                // Resetear las banderas de acción seleccionada
                team1ActionSelected = false;
                team2ActionSelected = false;
                System.out.println("Servidor: Banderas de acción seleccionada reseteadas. Equipo 1: " + team1ActionSelected + ", Equipo 2: " + team2ActionSelected);
                
                return response;
            } catch (Exception e) {
                response.put("error", "Error al procesar el turno: " + e.getMessage());
                return response;
            }
        }
    }
} 