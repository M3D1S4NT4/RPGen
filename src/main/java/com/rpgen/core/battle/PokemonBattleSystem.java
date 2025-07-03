package com.rpgen.core.battle;

import com.rpgen.core.entity.Entity;
import com.rpgen.pokemon.Pokemon;
import com.rpgen.core.action.GameAction;
import java.util.*;
import com.rpgen.pokemon.HeldItem;

public class PokemonBattleSystem extends BaseBattleSystem {
    private Entity activePokemon1;
    private Entity activePokemon2;
    private Map<Entity, List<GameAction>> pokemonMoves;
    private int turnCount;
    private Random random;
    private Map<Entity, GameAction> selectedMoves;
    // Mapa para restricción de movimiento por objeto tipo Choice
    private final Map<Pokemon, String> lockedMoveByPokemon = new HashMap<>();

    public PokemonBattleSystem() {
        super();
        this.pokemonMoves = new HashMap<>();
        this.selectedMoves = new HashMap<>();
        this.turnCount = 0;
        this.random = new Random();
    }

    @Override
    public void initialize(List<Entity> team1, List<Entity> team2) {
        super.initialize(team1, team2);
        this.activePokemon1 = team1.get(0);
        this.activePokemon2 = team2.get(0);
        this.turnCount = 0;
        this.lockedMoveByPokemon.clear();
        for (Entity pokemon : team1) {
            pokemonMoves.put(pokemon, pokemon.getAvailableActions());
        }
        for (Entity pokemon : team2) {
            pokemonMoves.put(pokemon, pokemon.getAvailableActions());
        }
    }

    public void selectMove(Entity pokemon, GameAction move) {
        // Lógica de restricción Choice
        if (pokemon instanceof Pokemon poke) {
            HeldItem item = poke.getHeldItem();
            if (item != null && Boolean.TRUE.equals(item.getExtraEffects().get("onlyOneMove"))) {
                String lockedMove = lockedMoveByPokemon.get(poke);
                if (lockedMove == null) {
                    // Primer movimiento: se bloquea
                    lockedMoveByPokemon.put(poke, move.getId());
                } else if (!lockedMove.equals(move.getId())) {
                    // Intento de cambiar: forzar el movimiento bloqueado
                    GameAction forced = pokemonMoves.get(pokemon).stream()
                        .filter(a -> a.getId().equals(lockedMove))
                        .findFirst().orElse(move);
                    selectedMoves.put(pokemon, forced);
                    return;
                }
            } else {
                // Si el objeto ya no es Choice, se resetea el bloqueo
                lockedMoveByPokemon.remove(poke);
            }
        }
        selectedMoves.put(pokemon, move);
    }

    public void switchPokemon(Entity newPokemon, boolean isTeam1) {
        if (isTeam1) {
            if (team1.contains(newPokemon)) {
                activePokemon1 = newPokemon;
                notifyPokemonSwitched(newPokemon, true);
            }
        } else {
            if (team2.contains(newPokemon)) {
                activePokemon2 = newPokemon;
                notifyPokemonSwitched(newPokemon, false);
            }
        }
        // Al cambiar de Pokémon, se resetea el bloqueo
        if (newPokemon instanceof Pokemon poke) {
            lockedMoveByPokemon.remove(poke);
        }
    }

    public Entity getActivePokemon(boolean isTeam1) {
        return isTeam1 ? activePokemon1 : activePokemon2;
    }

    @Override
    public void processTurn() {
        if (isBattleOver()) return;

        turnCount++;
        notifyTurnStart();

        Entity firstAttacker = activePokemon1.getSpeed() >= activePokemon2.getSpeed() ? 
            activePokemon1 : activePokemon2;
        Entity secondAttacker = firstAttacker == activePokemon1 ? activePokemon2 : activePokemon1;

        processAttacks(firstAttacker, secondAttacker);
        if (!isBattleOver()) {
            processAttacks(secondAttacker, firstAttacker);
        }

        selectedMoves.clear();
        checkBattleEnd();
    }

    private void processAttacks(Entity attacker, Entity defender) {
        if (!attacker.isAlive() || !defender.isAlive()) return;

        GameAction selectedMove = selectedMoves.get(attacker);
        if (selectedMove != null) {
            executeAttack(attacker, defender, selectedMove);
            notifyActionExecuted(attacker, defender, selectedMove);
        }
    }

    private void executeAttack(Entity attacker, Entity defender, GameAction action) {
        if (!(attacker instanceof Pokemon) || !(defender instanceof Pokemon)) return;
        
        Pokemon attackerPokemon = (Pokemon) attacker;
        Pokemon defenderPokemon = (Pokemon) defender;
        
        // Obtener el tipo del ataque del mapa de acción
        String attackType = (String) action.getProperties().get("type");
        if (attackType == null) return;

        // Calcular la efectividad del tipo
        double typeEffectiveness = calculateTypeEffectiveness(attackType, defenderPokemon.getTypes());
        
        // Calcular el daño base
        int baseDamage = calculateBaseDamage(attackerPokemon, defenderPokemon, action);
        
        // Aplicar modificadores
        double damage = baseDamage * typeEffectiveness;
        
        // Aplicar variación aleatoria (85-100%)
        damage *= (0.85 + (random.nextDouble() * 0.15));
        
        // Redondear el daño final
        int finalDamage = (int) Math.round(damage);
        
        // Aplicar el daño
        defenderPokemon.takeDamage(finalDamage);
        
        // Notificar la efectividad del tipo
        notifyTypeEffectiveness(typeEffectiveness);
    }

    private int calculateBaseDamage(Pokemon attacker, Pokemon defender, GameAction action) {
        // Obtener las propiedades del ataque
        Map<String, Object> properties = action.getProperties();
        String category = (String) properties.get("category");
        int power = (int) properties.getOrDefault("power", 0);
        
        // Calcular el ataque y defensa según la categoría
        int attackStat = category.equals("special") ? attacker.getSpecialAttack() : attacker.getAttack();
        int defenseStat = category.equals("special") ? defender.getSpecialDefense() : defender.getDefense();
        
        // Fórmula de daño simplificada
        return (int) ((((2 * 50 / 5 + 2) * attackStat * power / defenseStat) / 50) + 2);
    }

    private void notifyPokemonSwitched(Entity pokemon, boolean isTeam1) {
        for (BattleListener listener : getListeners()) {
            if (listener instanceof PokemonBattleListener) {
                ((PokemonBattleListener) listener).onPokemonSwitched(pokemon, isTeam1);
            }
        }
    }

    private void notifyTypeEffectiveness(double effectiveness) {
        for (BattleListener listener : getListeners()) {
            if (listener instanceof PokemonBattleListener) {
                ((PokemonBattleListener) listener).onTypeEffectiveness(effectiveness);
            }
        }
    }

    private List<BattleListener> getListeners() {
        return new ArrayList<>();
    }

    private double calculateTypeEffectiveness(String attackType, List<String> defenderTypes) {
        return TypeEffectiveness.getEffectiveness(attackType, defenderTypes);
    }
} 