package com.rpgen.core.battle;

import com.rpgen.core.entity.Entity;
import com.rpgen.core.action.GameAction;
import com.rpgen.core.action.CombatCommand;
import com.rpgen.core.action.AbstractCombatCommand;
import java.util.*;

public abstract class BattleEngine<E extends Entity, A extends GameAction> {
    protected List<E> team1;
    protected List<E> team2;
    private List<CombatCommand> pendingCommands;
    private List<BattleListener> listeners;
    private boolean battleOver;

    public BattleEngine() {
        this.pendingCommands = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.battleOver = false;
    }

    public void initialize(List<E> team1, List<E> team2) {
        this.team1 = new ArrayList<>(team1);
        this.team2 = new ArrayList<>(team2);
        this.pendingCommands.clear();
        this.battleOver = false;
    }

    public void addAction(E source, E target, A action) {
        pendingCommands.add(new AbstractCombatCommand(source, target, action.getName(), action) {
            @Override
            public void execute() {
                if (!canExecute() || action == null) return;
                action.execute(source, target);
            }
        });
    }

    public void addCommand(CombatCommand command) {
        pendingCommands.add(command);
    }

    public void processTurn() {
        if (battleOver) return;

        for (CombatCommand command : pendingCommands) {
            if (!command.canExecute()) {
                continue;
            }

            command.execute();
            notifyActionExecuted((E) command.getSource(), (E) command.getTarget(), (A) command.getAction());
        }

        pendingCommands.clear();
        checkBattleEnd();
    }

    public List<E> getActiveEntities() {
        List<E> activeEntities = new ArrayList<>();
        activeEntities.addAll(team1.stream().filter(Entity::isAlive).toList());
        activeEntities.addAll(team2.stream().filter(Entity::isAlive).toList());
        return activeEntities;
    }

    public boolean isBattleOver() {
        return battleOver;
    }

    public void registerBattleListener(BattleListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeBattleListener(BattleListener listener) {
        listeners.remove(listener);
    }

    protected void checkBattleEnd() {
        boolean team1Alive = team1.stream().anyMatch(Entity::isAlive);
        boolean team2Alive = team2.stream().anyMatch(Entity::isAlive);

        if (!team1Alive || !team2Alive) {
            battleOver = true;
        }
    }

    protected void notifyActionExecuted(E source, E target, A action) {
        for (BattleListener listener : listeners) {
            listener.onActionExecuted(source, target, action);
        }
    }

    protected void notifyTurnStart() {
        for (BattleListener listener : listeners) {
            listener.onTurnStart();
        }
    }

    public List<E> getTeam1() {
        return team1;
    }

    public List<E> getTeam2() {
        return team2;
    }
} 