package com.rpgen.pokemon.entity;

import com.rpgen.core.entity.Entity;
public class DamageMove extends PokemonMove {

    public DamageMove(String id, String name, String type, String category, int power, int accuracy, String description) {
        super(id, name, type, category, power, accuracy, description, 0);
    }

    @Override
    public void execute(Entity source, Entity target) {
        // La lógica de daño se maneja en PokemonBattleSystem
        // Este método se mantiene vacío ya que el daño se calcula y aplica en el sistema de batalla
    }
} 