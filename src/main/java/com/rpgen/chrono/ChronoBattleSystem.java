package com.rpgen.chrono;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChronoBattleSystem {
    private List<ChronoEntity> allies;
    private List<ChronoEntity> enemies;
    private List<ChronoBattleListener> listeners;
    private Timer atbTimer;
    private boolean battleActive;
    private static final int ATB_TICK_MS = 100; // 100 ms por tick

    // --- NUEVO: Mensajes de batalla ---
    private List<String> messages = new ArrayList<>();

    // --- NUEVO: Contador de turnos para enemigos ---
    private java.util.Map<ChronoEntity, Integer> enemyTurnCounters = new java.util.HashMap<>();
    private java.util.Map<ChronoEntity, String> enemySpecialAttacks = new java.util.HashMap<>(); // nombre del ataque especial

    public ChronoBattleSystem(List<ChronoEntity> allies, List<ChronoEntity> enemies) {
        this.allies = allies;
        this.enemies = enemies;
        this.listeners = new ArrayList<>();
        this.battleActive = false;
    }

    public void addListener(ChronoBattleListener listener) {
        listeners.add(listener);
    }

    public void startBattle() {
        if (battleActive) return;
        battleActive = true;
        for (ChronoEntity entity : getAllEntities()) {
            entity.resetATB();
            entity.setAtbCounter(entity.getAtbMax());
            entity.setCanAct(true);
            // Aplicar efectos de equipamiento
            ChronoEquipmentEffects.applyEquipmentEffects(entity);
        }
        // Inicializar contadores de turnos de enemigos
        for (ChronoEntity enemy : enemies) {
            enemyTurnCounters.put(enemy, 0);
            // Cargar ataques especiales desde el JSON
            loadEnemySpecialAttack(enemy);
        }
        atbTimer = new Timer();
        atbTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateATB();
            }
        }, 0, ATB_TICK_MS);
    }

    public void stopBattle() {
        battleActive = false;
        if (atbTimer != null) {
            atbTimer.cancel();
        }
    }

    private void updateATB() {
        if (!battleActive) return;
        // Avanzar el ATB de cada entidad
        for (ChronoEntity entity : getAllEntities()) {
            entity.advanceATB();
        }
    }

    private List<ChronoEntity> getAllEntities() {
        List<ChronoEntity> all = new ArrayList<>();
        all.addAll(allies);
        all.addAll(enemies);
        return all;
    }

    // --- NUEVO: Getter para mensajes de batalla ---
    public List<String> getMessages() {
        return messages;
    }

    // --- NUEVO: Ejecutar acción de combate ---
    public void performAction(ChronoEntity actor, ChronoMove move, ChronoEntity target) {
        if (actor == null || move == null || target == null) return;
        // Si es técnica doble o triple, los personajes involucrados pierden su turno
        if (("double".equals(move.getMoveType()) || "triple".equals(move.getMoveType())) && move.getRequiredCharacters() != null) {
            for (String name : move.getRequiredCharacters()) {
                ChronoEntity involved = findEntityByName(name, allies);
                if (involved != null) involved.resetATB();
            }
        }
        
        int actorStrength = 0;
        if (actor.getStatsByLevel() != null && !actor.getStatsByLevel().isEmpty()) {
            actorStrength = actor.getStatsByLevel().get(0).getStrength();
        }
        // Sumar bono de arma/accesorio
        actorStrength += actor.getTotalBonus("attack");
        
        // Aplicar multiplicador de frenesí si está activo
        if (ChronoEquipmentEffects.isInFrenzyMode(actor)) {
            double frenzyMultiplier = ChronoEquipmentEffects.getFrenzyAttackMultiplier(actor);
            actorStrength = (int) (actorStrength * frenzyMultiplier);
        }
        
        int targetStamina = 0;
        if (target.getStatsByLevel() != null && !target.getStatsByLevel().isEmpty()) {
            targetStamina = target.getStatsByLevel().get(0).getStamina();
        }
        targetStamina += target.getTotalBonus("defense");
        int damage = Math.max(1, move.getPower() + actorStrength - targetStamina);
        
        // Aplicar modificadores de daño basados en equipamiento
        String damageType = move.getType(); // Usar el tipo del movimiento
        if (damageType != null) {
            double damageModifier = ChronoEquipmentEffects.calculateDamageModifier(target, damageType);
            damage = (int) (damage * damageModifier);
            
            if (damageModifier == 0.0) {
                String msg = target.getCharacter() + " absorbe completamente el daño " + damageType + "!";
                messages.add(msg);
            } else if (damageModifier < 1.0) {
                String msg = target.getCharacter() + " resiste el daño " + damageType + ".";
                messages.add(msg);
            }
        }
        
        int oldHp = target.getHp();
        target.setHp(Math.max(0, target.getHp() - damage));
        
        // Verificar auto-revive
        if (target.getHp() <= 0 && ChronoEquipmentEffects.hasAutoRevive(target)) {
            ChronoEquipmentEffects.consumeAutoRevive(target);
            target.setHp(target.getStatsByLevel().get(0).getHP() / 2); // Revive con 50% HP
            String msg = target.getCharacter() + " se revive automáticamente!";
            messages.add(msg);
        }
        
        // Verificar contraataque
        if (target.getHp() > 0 && ChronoEquipmentEffects.hasCounterAttack(target)) {
            int counterDamage = Math.max(1, target.getTotalBonus("attack") / 2);
            actor.setHp(Math.max(0, actor.getHp() - counterDamage));
            String msg = target.getCharacter() + " contraataca y causa " + counterDamage + " de daño!";
            messages.add(msg);
        }
        
        if (move.getCost() > 0) {
            int modifiedCost = ChronoEquipmentEffects.calculateModifiedMPCost(actor, move.getCost());
            actor.setMp(Math.max(0, actor.getMp() - modifiedCost));
        }
        String msg = actor.getCharacter() + " usa " + move.getName() + " contra " + target.getCharacter() + " y causa " + damage + " de daño.";
        if (target.getHp() <= 0 && oldHp > 0) {
            msg += " ¡" + target.getCharacter() + " ha sido derrotado!";
        }
        messages.add(msg);
        // Resetear ATB solo del actor
        actor.resetATB();
        // Si el actor es aliado, hacer que los enemigos ataquen automáticamente
        if (allies.contains(actor)) {
            enemyAutoAttack();
        }
    }

    // --- NUEVO: Lógica de ataque automático de enemigos ---
    private void enemyAutoAttack() {
        // Hacer que todos los enemigos vivos ataquen
        List<ChronoEntity> aliveEnemies = new ArrayList<>();
        for (ChronoEntity enemy : enemies) {
            if (enemy.getHp() > 0) {
                aliveEnemies.add(enemy);
            }
        }
        
        if (aliveEnemies.isEmpty()) return;
        messages.add("--------------------"); // Separador visual antes de ataques enemigos
        for (ChronoEntity enemy : aliveEnemies) {
            int turn = enemyTurnCounters.getOrDefault(enemy, 0) + 1;
            enemyTurnCounters.put(enemy, turn);
            // Elegir objetivo: el aliado con más HP
            ChronoEntity target = getAllyWithLeastHP();
            if (target == null) continue;
            ChronoMove moveToUse = null;
            if (turn % 3 == 0 && enemySpecialAttacks.get(enemy) != null) {
                // Usar ataque especial cada 3 turnos
                moveToUse = createSpecialMove(enemySpecialAttacks.get(enemy));
            } else {
                moveToUse = createBasicMove();
            }
            performAction(enemy, moveToUse, target);
        }
        messages.add("--------------------"); // Separador visual después de ataques enemigos
    }

    // --- NUEVO: Obtener aliado con más HP ---
    private ChronoEntity getAllyWithLeastHP() {
        ChronoEntity target = null;
        // Buscar el valor mínimo de HP entre los aliados vivos
        int minHP = 999;
        for (ChronoEntity ally : allies) {
            if (ally.getHp() > 0 && ally.getHp() < minHP) {
                minHP = ally.getHp();
            }
        }
        // Ahora buscar el primer aliado con ese HP mínimo
        for (ChronoEntity ally : allies) {
            if (ally.getHp() > 0 && ally.getHp() == minHP) {
                target = ally;
                break;
            }
        }
        return target;
    }

    // --- NUEVO: Crear movimientos básicos y especiales ---
    private ChronoMove createBasicMove() {
        return new ChronoMove("Attack", "physical", 10, 0, "One enemy");
    }

    private ChronoMove createSpecialMove(String name) {
        // Mapear nombres de ataques especiales a movimientos
        switch (name.toLowerCase()) {
            case "laser":
                return new ChronoMove("Laser", "shadow", 50, 0, "One enemy");
            case "darkmatter":
                return new ChronoMove("DarkMatter", "shadow", 80, 0, "All enemies");
            case "fire":
                return new ChronoMove("Fire", "fire", 40, 0, "One enemy");
            case "ice":
                return new ChronoMove("Ice", "ice", 40, 0, "One enemy");
            case "lightning":
                return new ChronoMove("Lightning", "lightning", 40, 0, "One enemy");
            default:
                return new ChronoMove(name, "physical", 30, 0, "One enemy");
        }
    }

    // --- NUEVO: Cargar ataques especiales de enemigos ---
    private void loadEnemySpecialAttack(ChronoEntity enemy) {
        try {
            // Aquí deberías cargar desde chrono-enemies-attacks.json
            // Por ahora, asignar ataques especiales básicos
            String enemyName = enemy.getCharacter().toLowerCase();
            if (enemyName.contains("lavos")) {
                enemySpecialAttacks.put(enemy, "Laser");
            } else if (enemyName.contains("magus")) {
                enemySpecialAttacks.put(enemy, "DarkMatter");
            } else {
                enemySpecialAttacks.put(enemy, "Attack"); // Ataque básico
            }
        } catch (Exception e) {
            enemySpecialAttacks.put(enemy, "Attack");
        }
    }

    private ChronoEntity findEntityByName(String name, java.util.List<ChronoEntity> list) {
        for (ChronoEntity e : list) {
            if (e.getCharacter() != null && e.getCharacter().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }
} 