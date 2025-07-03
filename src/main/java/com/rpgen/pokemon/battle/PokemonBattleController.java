package com.rpgen.pokemon.battle;

import com.rpgen.core.entity.Entity;
import com.rpgen.pokemon.entity.Pokemon;
import com.rpgen.core.action.GameAction;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;
import java.lang.reflect.*;

@RestController
@RequestMapping("/api/pokemon-battle")
public class PokemonBattleController {
    private PokemonBattleEngine battleSystem;
    private Map<String, PokemonBattleEngine> activeBattles;

    public PokemonBattleController() {
        this.activeBattles = new HashMap<>();
    }

    @PostMapping("/start")
    public ResponseEntity<?> startBattle(@RequestBody BattleRequest request) {
        try {
            battleSystem = new PokemonBattleEngine();
            List<Map<String, Object>> team1 = new ArrayList<>();
            for (Entity e : request.getTeam1()) team1.add(entityToMap(e));
            List<Map<String, Object>> team2 = new ArrayList<>();
            for (Entity e : request.getTeam2()) team2.add(entityToMap(e));
            battleSystem.initialize(team1, team2);
            String battleId = UUID.randomUUID().toString();
            activeBattles.put(battleId, battleSystem);
            return ResponseEntity.ok(new BattleResponse(battleId, "Batalla iniciada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{battleId}/action")
    public ResponseEntity<?> performAction(
            @PathVariable String battleId,
            @RequestBody ActionRequest request) {
        try {
            PokemonBattleEngine battle = activeBattles.get(battleId);
            if (battle == null) {
                return ResponseEntity.notFound().build();
            }
            Map<String, Object> source = entityToMap(request.getSource());
            Map<String, Object> target = entityToMap(request.getTarget());
            Map<String, Object> action = gameActionToMap(request.getAction());
            battle.addAction(source, target, action);
            return ResponseEntity.ok(new ActionResponse("Acción registrada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{battleId}/switch")
    public ResponseEntity<?> switchPokemon(
            @PathVariable String battleId,
            @RequestBody SwitchRequest request) {
        try {
            PokemonBattleEngine battle = activeBattles.get(battleId);
            if (battle == null) {
                return ResponseEntity.notFound().build();
            }
            Map<String, Object> newPokemon = entityToMap(request.getNewPokemon());
            battle.switchPokemon(newPokemon, request.isTeam1());
            return ResponseEntity.ok(new SwitchResponse("Pokémon cambiado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{battleId}/process-turn")
    public ResponseEntity<?> processTurn(@PathVariable String battleId) {
        try {
            PokemonBattleEngine battle = activeBattles.get(battleId);
            if (battle == null) {
                return ResponseEntity.notFound().build();
            }

            battle.processTurn();
            return ResponseEntity.ok(new TurnResponse("Turno procesado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Clases de request/response
    private static class BattleRequest {
        private List<Entity> team1;
        private List<Entity> team2;

        public List<Entity> getTeam1() { return team1; }
        public List<Entity> getTeam2() { return team2; }
    }

    private static class BattleResponse {
        private String battleId;
        private String message;

        public BattleResponse(String battleId, String message) {
            this.battleId = battleId;
            this.message = message;
        }
    }

    private static class ActionRequest {
        private Pokemon source;
        private Pokemon target;
        private GameAction action;

        public Pokemon getSource() { return source; }
        public Pokemon getTarget() { return target; }
        public GameAction getAction() { return action; }
    }

    private static class ActionResponse {
        private String message;

        public ActionResponse(String message) {
            this.message = message;
        }
    }

    private static class SwitchRequest {
        private Pokemon newPokemon;
        private boolean isTeam1;

        public Pokemon getNewPokemon() { return newPokemon; }
        public boolean isTeam1() { return isTeam1; }
    }

    private static class SwitchResponse {
        private String message;

        public SwitchResponse(String message) {
            this.message = message;
        }
    }

    private static class TurnResponse {
        private String message;

        public TurnResponse(String message) {
            this.message = message;
        }
    }

    private static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }

    // Métodos utilitarios para convertir objetos a Map<String, Object>
    private static Map<String, Object> entityToMap(Object obj) {
        if (obj == null) return null;
        Map<String, Object> map = new HashMap<>();
        for (Method m : obj.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && m.getName().startsWith("get") && !m.getName().equals("getClass")) {
                try {
                    Object value = m.invoke(obj);
                    String key = Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);
                    map.put(key, value);
                } catch (Exception ignored) {}
            }
            if (m.getParameterCount() == 0 && m.getName().startsWith("is")) {
                try {
                    Object value = m.invoke(obj);
                    String key = Character.toLowerCase(m.getName().charAt(2)) + m.getName().substring(3);
                    map.put(key, value);
                } catch (Exception ignored) {}
            }
        }
        return map;
    }
    private static Map<String, Object> gameActionToMap(GameAction action) {
        if (action == null) return null;
        Map<String, Object> map = entityToMap(action);
        // Si GameAction tiene propiedades adicionales, agrégalas aquí
        return map;
    }
} 