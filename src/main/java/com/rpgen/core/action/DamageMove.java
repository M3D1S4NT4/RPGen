package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;
import com.rpgen.core.battle.TypeEffectiveness;
import java.util.Random;

public class DamageMove extends PokemonMove {
    private static final Random random = new Random();

    public DamageMove(String id, String name, String type, String category, int power, int accuracy, String description) {
        super(id, name, type, category, power, accuracy, description, 0);
    }

    @Override
    public void execute(Entity source, Entity target) {
        // La lógica de daño se maneja en PokemonBattleSystem
        // Este método se mantiene vacío ya que el daño se calcula y aplica en el sistema de batalla
    }
} 