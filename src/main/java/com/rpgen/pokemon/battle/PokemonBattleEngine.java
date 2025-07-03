package com.rpgen.pokemon.battle;

import com.rpgen.core.battle.BattleEngine;
import com.rpgen.core.battle.BattleListener;
import java.util.*;

public class PokemonBattleEngine implements BattleEngine<Map<String, Object>, Map<String, Object>> {
    private List<Map<String, Object>> team1;
    private List<Map<String, Object>> team2;
    private List<Map<String, Object>> pendingActions;
    private Random random;
    private boolean team1ActionSelected;
    private boolean team2ActionSelected;
    private boolean battleOver;
    private Map<String, Object> team1ActivePokemon;
    private Map<String, Object> team2ActivePokemon;
    private final Map<String, String> lockedMoveByPokemonId = new HashMap<>();
    private final Map<String, Map<String, Object>> selectedMoves = new HashMap<>();

    public PokemonBattleEngine() {
        this.pendingActions = new ArrayList<>();
        this.random = new Random();
        this.team1ActionSelected = false;
        this.team2ActionSelected = false;
        this.battleOver = false;
    }

    @Override
    public void initialize(List<Map<String, Object>> team1, List<Map<String, Object>> team2) {
        this.team1 = new ArrayList<>(team1);
        this.team2 = new ArrayList<>(team2);
        this.pendingActions = new ArrayList<>();
        this.team1ActionSelected = false;
        this.team2ActionSelected = false;
        this.battleOver = false;
        this.team1ActivePokemon = null;
        this.team2ActivePokemon = null;
    }

    @Override
    public void addAction(Map<String, Object> source, Map<String, Object> target, Map<String, Object> action) {
        if (battleOver) return;
        Map<String, Object> sourceInTeam = findPokemonInTeams(source);
        if (sourceInTeam == null || ((Number) sourceInTeam.getOrDefault("health", 0)).intValue() <= 0) {
            return;
        }
        pendingActions.add(Map.of(
            "source", source,
            "target", target,
            "action", action
        ));
        if (team1.stream().anyMatch(p -> p.get("id").equals(source.get("id")))) {
            team1ActionSelected = true;
        } else {
            team2ActionSelected = true;
        }
    }

    @Override
    public void processTurn() {
        if (battleOver) return;
        if (team1ActivePokemon == null || team2ActivePokemon == null) return;
        Map<String, Object> move1 = selectedMoves.getOrDefault(team1ActivePokemon.get("id"), null);
        Map<String, Object> move2 = selectedMoves.getOrDefault(team2ActivePokemon.get("id"), null);
        if (move1 == null || move2 == null) {
            // Si no hay movimientos seleccionados, simplemente no procesar el turno especial
            selectedMoves.clear();
            return;
        }
        int speed1 = ((Number) team1ActivePokemon.getOrDefault("speed", 0)).intValue();
        int speed2 = ((Number) team2ActivePokemon.getOrDefault("speed", 0)).intValue();
        boolean firstIsTeam1 = speed1 >= speed2;
        if (firstIsTeam1) {
            processAttack(team1ActivePokemon, team2ActivePokemon, move1);
            if (!battleOver) processAttack(team2ActivePokemon, team1ActivePokemon, move2);
        } else {
            processAttack(team2ActivePokemon, team1ActivePokemon, move2);
            if (!battleOver) processAttack(team1ActivePokemon, team2ActivePokemon, move1);
        }
        selectedMoves.clear();
        checkBattleEnd();
    }

    @Override
    public boolean isBattleOver() {
        return battleOver;
    }

    @Override
    public List<Map<String, Object>> getTeam1() {
        return team1;
    }

    @Override
    public List<Map<String, Object>> getTeam2() {
        return team2;
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
        }
    }

    private Map<String, Object> findPokemonInTeams(Map<String, Object> pokemon) {
        for (Map<String, Object> p : team1) {
            if (p.get("id").equals(pokemon.get("id"))) {
                return p;
            }
        }
        for (Map<String, Object> p : team2) {
            if (p.get("id").equals(pokemon.get("id"))) {
                return p;
            }
        }
        return null;
    }

    public double calculateTypeEffectiveness(String attackType, List<String> defenderTypes) {
        return calculateTypeEffectivenessInternal(attackType, defenderTypes);
    }

    private double calculateTypeEffectivenessInternal(String attackType, List<String> defenderTypes) {
        double effectiveness = 1.0;
        Map<String, Map<String, Double>> typeChart = new HashMap<>();
        Map<String, Double> normal = new HashMap<>();
        normal.put("rock", 0.5); normal.put("ghost", 0.0); normal.put("steel", 0.5);
        typeChart.put("normal", normal);
        Map<String, Double> fire = new HashMap<>();
        fire.put("fire", 0.5); fire.put("water", 0.5); fire.put("grass", 2.0);
        fire.put("ice", 2.0); fire.put("bug", 2.0); fire.put("rock", 0.5);
        fire.put("dragon", 0.5); fire.put("steel", 2.0);
        typeChart.put("fire", fire);
        Map<String, Double> water = new HashMap<>();
        water.put("fire", 2.0); water.put("water", 0.5); water.put("grass", 0.5);
        water.put("ground", 2.0); water.put("rock", 2.0); water.put("dragon", 0.5);
        typeChart.put("water", water);
        Map<String, Double> electric = new HashMap<>();
        electric.put("water", 2.0); electric.put("electric", 0.5); electric.put("grass", 0.5);
        electric.put("ground", 0.0); electric.put("flying", 2.0); electric.put("dragon", 0.5);
        typeChart.put("electric", electric);
        Map<String, Double> grass = new HashMap<>();
        grass.put("fire", 0.5); grass.put("water", 2.0); grass.put("grass", 0.5);
        grass.put("poison", 0.5); grass.put("ground", 2.0); grass.put("flying", 0.5);
        grass.put("bug", 0.5); grass.put("rock", 2.0); grass.put("dragon", 0.5);
        grass.put("steel", 0.5);
        typeChart.put("grass", grass);
        Map<String, Double> ice = new HashMap<>();
        ice.put("fire", 0.5); ice.put("water", 0.5); ice.put("grass", 2.0);
        ice.put("ice", 0.5); ice.put("ground", 2.0); ice.put("flying", 2.0);
        ice.put("dragon", 2.0); ice.put("steel", 0.5);
        typeChart.put("ice", ice);
        Map<String, Double> fighting = new HashMap<>();
        fighting.put("normal", 2.0); fighting.put("ice", 2.0); fighting.put("poison", 0.5);
        fighting.put("flying", 0.5); fighting.put("psychic", 0.5); fighting.put("bug", 0.5);
        fighting.put("rock", 2.0); fighting.put("ghost", 0.0); fighting.put("dark", 2.0);
        fighting.put("steel", 2.0);
        typeChart.put("fighting", fighting);
        Map<String, Double> poison = new HashMap<>();
        poison.put("grass", 2.0); poison.put("poison", 0.5); poison.put("ground", 0.5);
        poison.put("rock", 0.5); poison.put("ghost", 0.5); poison.put("steel", 0.0);
        typeChart.put("poison", poison);
        Map<String, Double> ground = new HashMap<>();
        ground.put("fire", 2.0); ground.put("electric", 2.0); ground.put("grass", 0.5);
        ground.put("poison", 2.0); ground.put("flying", 0.0); ground.put("bug", 0.5);
        ground.put("rock", 2.0); ground.put("steel", 2.0);
        typeChart.put("ground", ground);
        Map<String, Double> flying = new HashMap<>();
        flying.put("electric", 0.5); flying.put("grass", 2.0); flying.put("fighting", 2.0);
        flying.put("bug", 2.0); flying.put("rock", 0.5); flying.put("steel", 0.5);
        typeChart.put("flying", flying);
        Map<String, Double> psychic = new HashMap<>();
        psychic.put("fighting", 2.0); psychic.put("poison", 2.0); psychic.put("psychic", 0.5);
        psychic.put("dark", 0.0); psychic.put("steel", 0.5);
        typeChart.put("psychic", psychic);
        Map<String, Double> bug = new HashMap<>();
        bug.put("fire", 0.5); bug.put("grass", 2.0); bug.put("fighting", 0.5);
        bug.put("poison", 0.5); bug.put("flying", 0.5); bug.put("psychic", 2.0);
        bug.put("ghost", 0.5); bug.put("dark", 2.0); bug.put("steel", 0.5);
        typeChart.put("bug", bug);
        Map<String, Double> rock = new HashMap<>();
        rock.put("fire", 2.0); rock.put("ice", 2.0); rock.put("fighting", 0.5);
        rock.put("ground", 0.5); rock.put("flying", 2.0); rock.put("bug", 2.0);
        rock.put("steel", 0.5);
        typeChart.put("rock", rock);
        Map<String, Double> ghost = new HashMap<>();
        ghost.put("normal", 0.0); ghost.put("psychic", 2.0); ghost.put("ghost", 2.0);
        ghost.put("dark", 0.5);
        typeChart.put("ghost", ghost);
        Map<String, Double> dragon = new HashMap<>();
        dragon.put("dragon", 2.0); dragon.put("steel", 0.5);
        typeChart.put("dragon", dragon);
        Map<String, Double> dark = new HashMap<>();
        dark.put("fighting", 0.5); dark.put("psychic", 2.0); dark.put("ghost", 2.0);
        dark.put("dark", 0.5);
        typeChart.put("dark", dark);
        Map<String, Double> steel = new HashMap<>();
        steel.put("fire", 0.5); steel.put("water", 0.5); steel.put("electric", 0.5);
        steel.put("ice", 2.0); steel.put("rock", 2.0); steel.put("steel", 0.5);
        typeChart.put("steel", steel);
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

    public void selectMove(Map<String, Object> pokemon, Map<String, Object> move) {
        if (pokemon == null || move == null) return;
        String pokeId = (String) pokemon.get("id");
        if (pokeId == null) return;
        Map<String, Object> heldItem = (Map<String, Object>) pokemon.get("heldItem");
        boolean isChoice = heldItem != null && Boolean.TRUE.equals(heldItem.getOrDefault("onlyOneMove", false));
        String lockedMove = lockedMoveByPokemonId.get(pokeId);
        String moveId = (String) move.get("id");
        if (isChoice) {
            if (lockedMove == null) {
                lockedMoveByPokemonId.put(pokeId, moveId);
            } else if (!lockedMove.equals(moveId)) {
                List<Map<String, Object>> moves = (List<Map<String, Object>>) pokemon.get("moves");
                Map<String, Object> forced = moves.stream()
                        .filter(a -> lockedMove.equals(a.get("id")))
                        .findFirst().orElse(move);
                selectedMoves.put(pokeId, forced);
                return;
            }
        } else {
            lockedMoveByPokemonId.remove(pokeId);
        }
        selectedMoves.put(pokeId, move);
    }

    public Map<String, Object> switchPokemon(Map<String, Object> newPokemon, boolean isTeam1) {
        if (battleOver) {
            return Map.of("error", "La batalla ha terminado");
        }
        List<Map<String, Object>> team = isTeam1 ? team1 : team2;
        for (int i = 0; i < team.size(); i++) {
            if (team.get(i).get("id").equals(newPokemon.get("id"))) {
                if (((Number) team.get(i).getOrDefault("health", 0)).intValue() <= 0) {
                    return Map.of("error", "No puedes cambiar a un Pokémon debilitado");
                }
                team.set(i, newPokemon);
                if (isTeam1) {
                    team1ActivePokemon = newPokemon;
                    team1ActionSelected = true;
                } else {
                    team2ActivePokemon = newPokemon;
                    team2ActionSelected = true;
                }
                // Resetear bloqueo Choice
                lockedMoveByPokemonId.remove(newPokemon.get("id"));
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

    public Map<String, Object> getActivePokemon(boolean isTeam1) {
        return isTeam1 ? team1ActivePokemon : team2ActivePokemon;
    }

    private void processAttack(Map<String, Object> attacker, Map<String, Object> defender, Map<String, Object> move) {
        if (attacker == null || defender == null || move == null) return;
        if (((Number) attacker.getOrDefault("health", 0)).intValue() <= 0 || ((Number) defender.getOrDefault("health", 0)).intValue() <= 0) return;
        String attackType = (String) move.get("type");
        if (attackType == null) return;
        List<String> defenderTypes = (List<String>) defender.getOrDefault("types", List.of("normal"));
        double typeEffectiveness = calculateTypeEffectiveness(attackType, defenderTypes);
        int baseDamage = calculateBaseDamage(attacker, defender, move);
        double damage = baseDamage * typeEffectiveness;
        damage *= (0.85 + (random.nextDouble() * 0.15));
        int finalDamage = (int) Math.round(damage);
        int currentHealth = ((Number) defender.getOrDefault("health", 0)).intValue();
        defender.put("health", Math.max(0, currentHealth - finalDamage));
    }

    private int calculateBaseDamage(Map<String, Object> attacker, Map<String, Object> defender, Map<String, Object> move) {
        String category = (String) move.getOrDefault("category", "physical");
        int power = ((Number) move.getOrDefault("power", 0)).intValue();
        int attackStat = category.equals("special") ? ((Number) attacker.getOrDefault("specialAttack", 50)).intValue() : ((Number) attacker.getOrDefault("attack", 50)).intValue();
        int defenseStat = category.equals("special") ? ((Number) defender.getOrDefault("specialDefense", 50)).intValue() : ((Number) defender.getOrDefault("defense", 50)).intValue();
        return (int) ((((2 * 50 / 5 + 2) * attackStat * power / defenseStat) / 50) + 2);
    }

    @Override
    public void registerBattleListener(BattleListener listener) {
        // Implementación vacía o lógica según tu necesidad
    }

    @Override
    public void removeBattleListener(BattleListener listener) {
        // Implementación vacía o lógica según tu necesidad
    }

    @Override
    public List<Map<String, Object>> getActiveEntities() {
        List<Map<String, Object>> actives = new ArrayList<>();
        if (team1ActivePokemon != null) actives.add(team1ActivePokemon);
        if (team2ActivePokemon != null) actives.add(team2ActivePokemon);
        return actives;
    }
} 