package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;
import com.rpgen.core.entity.Pokemon;
import java.util.Random;

public class StatusMove extends PokemonMove {
    private static final Random random = new Random();
    private final String statusEffect;

    public StatusMove(String id, String name, String type, String category, int power, int accuracy, 
                     String description, String statusEffect) {
        super(id, name, type, category, power, accuracy, description, 0);
        this.statusEffect = statusEffect;
        this.properties.put("statusEffect", statusEffect);
    }

    @Override
    public void execute(Entity source, Entity target) {
        if (!(target instanceof Pokemon)) return;
        
        Pokemon targetPokemon = (Pokemon) target;
        
        // Verificar precisión
        if (random.nextInt(100) >= accuracy) {
            return; // El movimiento falla
        }
        
        // Aplicar el efecto de estado
        switch (statusEffect.toLowerCase()) {
            case "paralysis":
                // Reducir velocidad
                targetPokemon.setSpeed((int)(targetPokemon.getSpeed() * 0.5));
                break;
            case "burn":
                // Reducir ataque
                targetPokemon.setAttack((int)(targetPokemon.getAttack() * 0.5));
                break;
            case "poison":
                // El daño por veneno se maneja en el sistema de batalla
                break;
            case "sleep":
                // El Pokémon no puede atacar
                break;
            case "freeze":
                // El Pokémon no puede atacar
                break;
            case "confusion":
                // El Pokémon puede atacarse a sí mismo
                break;
        }
    }
} 