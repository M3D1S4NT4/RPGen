package com.rpgen.core.entity;

import java.util.UUID;
import com.rpgen.core.action.GameAction;
import java.util.List;

public class Character extends BaseEntity {
    public Character(String name, int maxHealth, int attack, int defense) {
        super(UUID.randomUUID().toString(), name, maxHealth, attack, defense, 0);
    }

    @Override
    public String toString() {
        return String.format("%s (HP: %d/%d, ATK: %d, DEF: %d)", 
            getName(), getHealth(), getMaxHealth(), getAttack(), getDefense());
    }
} 