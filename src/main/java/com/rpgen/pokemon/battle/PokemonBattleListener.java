package com.rpgen.pokemon.battle;

import com.rpgen.core.battle.BattleListener;
import com.rpgen.core.entity.Entity;

public interface PokemonBattleListener extends BattleListener {
    void onPokemonSwitched(Entity pokemon, boolean isTeam1);
    void onMoveSelected(Entity pokemon, String moveName);
    void onTypeEffectiveness(double effectiveness);
    void onStatusEffectApplied(Entity pokemon, String status);
    void onStatusEffectRemoved(Entity pokemon, String status);
} 