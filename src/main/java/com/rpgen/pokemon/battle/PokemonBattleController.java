package com.rpgen.pokemon.battle;

import com.rpgen.core.entity.Entity;
import com.rpgen.pokemon.entity.Pokemon;
import com.rpgen.pokemon.entity.PokemonMove;
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
            List<Pokemon> team1 = request.getTeam1();
            List<Pokemon> team2 = request.getTeam2();
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
            battle.addAction(request.getSource(), request.getTarget(), (PokemonMove) request.getAction());
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
            battle.switchPokemon(request.getNewPokemon(), request.isTeam1());
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
        private List<Pokemon> team1;
        private List<Pokemon> team2;

        public List<Pokemon> getTeam1() { return team1; }
        public List<Pokemon> getTeam2() { return team2; }
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
} 