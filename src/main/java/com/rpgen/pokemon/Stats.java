package com.rpgen.pokemon;

import java.util.Map;
import java.util.HashMap;

public class Stats {
    private final int hp;
    private final int attack;
    private final int defense;
    private final int specialAttack;
    private final int specialDefense;
    private final int speed;

    // IVs (Individual Values) - valores de 0 a 31
    private final int hpIV;
    private final int attackIV;
    private final int defenseIV;
    private final int specialAttackIV;
    private final int specialDefenseIV;
    private final int speedIV;

    // EVs (Effort Values) - valores de 0 a 252 por estadística, máximo 510 total
    private final int hpEV;
    private final int attackEV;
    private final int defenseEV;
    private final int specialAttackEV;
    private final int specialDefenseEV;
    private final int speedEV;

    public Stats(int hp, int attack, int defense, int specialAttack, int specialDefense, int speed,
                 int hpIV, int attackIV, int defenseIV, int specialAttackIV, int specialDefenseIV, int speedIV,
                 int hpEV, int attackEV, int defenseEV, int specialAttackEV, int specialDefenseEV, int speedEV) {
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        
        this.hpIV = Math.max(0, Math.min(31, hpIV));
        this.attackIV = Math.max(0, Math.min(31, attackIV));
        this.defenseIV = Math.max(0, Math.min(31, defenseIV));
        this.specialAttackIV = Math.max(0, Math.min(31, specialAttackIV));
        this.specialDefenseIV = Math.max(0, Math.min(31, specialDefenseIV));
        this.speedIV = Math.max(0, Math.min(31, speedIV));
        
        this.hpEV = Math.max(0, Math.min(252, hpEV));
        this.attackEV = Math.max(0, Math.min(252, attackEV));
        this.defenseEV = Math.max(0, Math.min(252, defenseEV));
        this.specialAttackEV = Math.max(0, Math.min(252, specialAttackEV));
        this.specialDefenseEV = Math.max(0, Math.min(252, specialDefenseEV));
        this.speedEV = Math.max(0, Math.min(252, speedEV));
    }

    // Constructor con IVs y EVs por defecto
    public Stats(int hp, int attack, int defense, int specialAttack, int specialDefense, int speed) {
        this(hp, attack, defense, specialAttack, specialDefense, speed,
             31, 31, 31, 31, 31, 31, // IVs perfectos por defecto
             0, 0, 0, 0, 0, 0); // EVs en 0 por defecto
    }

    // Getters para estadísticas base
    public int getHp() { return hp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpecialAttack() { return specialAttack; }
    public int getSpecialDefense() { return specialDefense; }
    public int getSpeed() { return speed; }

    // Getters para IVs
    public int getHpIV() { return hpIV; }
    public int getAttackIV() { return attackIV; }
    public int getDefenseIV() { return defenseIV; }
    public int getSpecialAttackIV() { return specialAttackIV; }
    public int getSpecialDefenseIV() { return specialDefenseIV; }
    public int getSpeedIV() { return speedIV; }

    // Getters para EVs
    public int getHpEV() { return hpEV; }
    public int getAttackEV() { return attackEV; }
    public int getDefenseEV() { return defenseEV; }
    public int getSpecialAttackEV() { return specialAttackEV; }
    public int getSpecialDefenseEV() { return specialDefenseEV; }
    public int getSpeedEV() { return speedEV; }

    // Calcular estadísticas finales con IVs y EVs
    public int calculateFinalHP(int level) {
        return ((2 * hp + hpIV + hpEV / 4) * level) / 100 + level + 10;
    }

    public int calculateFinalStat(String stat, int level, double natureModifier) {
        int baseStat = 0;
        int iv = 0;
        int ev = 0;
        
        switch (stat.toLowerCase()) {
            case "attack":
                baseStat = attack;
                iv = attackIV;
                ev = attackEV;
                break;
            case "defense":
                baseStat = defense;
                iv = defenseIV;
                ev = defenseEV;
                break;
            case "specialattack":
            case "special_attack":
                baseStat = specialAttack;
                iv = specialAttackIV;
                ev = specialAttackEV;
                break;
            case "specialdefense":
            case "special_defense":
                baseStat = specialDefense;
                iv = specialDefenseIV;
                ev = specialDefenseEV;
                break;
            case "speed":
                baseStat = speed;
                iv = speedIV;
                ev = speedEV;
                break;
            default:
                return 0;
        }
        
        return (int) ((((2 * baseStat + iv + ev / 4) * level) / 100 + 5) * natureModifier);
    }

    public int getTotalEVs() {
        return hpEV + attackEV + defenseEV + specialAttackEV + specialDefenseEV + speedEV;
    }

    public boolean isValidEVs() {
        return getTotalEVs() <= 510;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("hp", hp);
        map.put("attack", attack);
        map.put("defense", defense);
        map.put("specialAttack", specialAttack);
        map.put("specialDefense", specialDefense);
        map.put("speed", speed);
        map.put("hpIV", hpIV);
        map.put("attackIV", attackIV);
        map.put("defenseIV", defenseIV);
        map.put("specialAttackIV", specialAttackIV);
        map.put("specialDefenseIV", specialDefenseIV);
        map.put("speedIV", speedIV);
        map.put("hpEV", hpEV);
        map.put("attackEV", attackEV);
        map.put("defenseEV", defenseEV);
        map.put("specialAttackEV", specialAttackEV);
        map.put("specialDefenseEV", specialDefenseEV);
        map.put("speedEV", speedEV);
        return map;
    }

    @Override
    public String toString() {
        return String.format("Stats{HP: %d, ATK: %d, DEF: %d, SP.ATK: %d, SP.DEF: %d, SPD: %d, " +
                           "IVs: %d/%d/%d/%d/%d/%d, EVs: %d/%d/%d/%d/%d/%d}",
                           hp, attack, defense, specialAttack, specialDefense, speed,
                           hpIV, attackIV, defenseIV, specialAttackIV, specialDefenseIV, speedIV,
                           hpEV, attackEV, defenseEV, specialAttackEV, specialDefenseEV, speedEV);
    }
} 