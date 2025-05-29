package com.rpgen.core.battle;

import com.rpgen.core.entity.Entity;
import com.rpgen.core.action.GameAction;
import com.rpgen.core.action.CombatCommand;
import com.rpgen.core.action.AbstractCombatCommand;
import java.util.*;

public class BaseBattleSystem implements BattleSystem {
    private List<Entity> team1;
    private List<Entity> team2;
    private List<CombatCommand> pendingCommands;
    private List<BattleListener> listeners;
    private boolean battleOver;

    public BaseBattleSystem() {
        this.pendingCommands = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.battleOver = false;
    }

    @Override
    public void initialize(List<Entity> team1, List<Entity> team2) {
        this.team1 = new ArrayList<>(team1);
        this.team2 = new ArrayList<>(team2);
        this.pendingCommands.clear();
        this.battleOver = false;
    }

    @Override
    public void addAction(Entity source, Entity target, GameAction action) {
        // Este método se mantiene por compatibilidad
        pendingCommands.add(new AbstractCombatCommand(source, target, action.getName(), action) {});
    }

    public void addCommand(CombatCommand command) {
        pendingCommands.add(command);
    }

    @Override
    public void processTurn() {
        if (battleOver) return;

        // Procesar todos los comandos pendientes
        for (CombatCommand command : pendingCommands) {
            if (!command.canExecute()) {
                continue;
            }

            command.execute();
            notifyActionExecuted(command.getSource(), command.getTarget(), null);
        }

        // Limpiar comandos pendientes
        pendingCommands.clear();

        // Verificar si la batalla ha terminado
        checkBattleEnd();
    }

    @Override
    public List<Entity> getActiveEntities() {
        List<Entity> activeEntities = new ArrayList<>();
        activeEntities.addAll(team1.stream().filter(Entity::isAlive).toList());
        activeEntities.addAll(team2.stream().filter(Entity::isAlive).toList());
        return activeEntities;
    }

    @Override
    public boolean isBattleOver() {
        return battleOver;
    }

    @Override
    public void registerBattleListener(BattleListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeBattleListener(BattleListener listener) {
        listeners.remove(listener);
    }

    private void checkBattleEnd() {
        boolean team1Alive = team1.stream().anyMatch(Entity::isAlive);
        boolean team2Alive = team2.stream().anyMatch(Entity::isAlive);

        if (!team1Alive || !team2Alive) {
            battleOver = true;
        }
    }

    private void notifyActionExecuted(Entity source, Entity target, GameAction action) {
        for (BattleListener listener : listeners) {
            listener.onActionExecuted(source, target, action);
        }
    }
} 