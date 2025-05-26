package com.rpgen.core.battle;

import com.rpgen.core.entity.Entity;
import com.rpgen.core.action.GameAction;
import java.util.*;


public class BaseBattleSystem implements BattleSystem {
    private List<Entity> team1;
    private List<Entity> team2;
    private List<BattleAction> pendingActions;
    private List<BattleListener> listeners;
    private boolean battleOver;

    public BaseBattleSystem() {
        this.pendingActions = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.battleOver = false;
    }

    @Override
    public void initialize(List<Entity> team1, List<Entity> team2) {
        this.team1 = new ArrayList<>(team1);
        this.team2 = new ArrayList<>(team2);
        this.pendingActions.clear();
        this.battleOver = false;
    }

    @Override
    public void addAction(Entity source, Entity target, GameAction action) {
        pendingActions.add(new BattleAction(source, target, action));
    }

    @Override
    public void processTurn() {
        if (battleOver) return;

        // Procesar todas las acciones pendientes
        for (BattleAction battleAction : pendingActions) {
            if (!battleAction.source.isAlive() || !battleAction.target.isAlive()) {
                continue;
            }

            battleAction.action.execute(battleAction.source, battleAction.target);
            notifyActionExecuted(battleAction.source, battleAction.target, battleAction.action);
        }

        // Limpiar acciones pendientes
        pendingActions.clear();

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

    private static class BattleAction {
        final Entity source;
        final Entity target;
        final GameAction action;

        BattleAction(Entity source, Entity target, GameAction action) {
            this.source = source;
            this.target = target;
            this.action = action;
        }
    }
} 