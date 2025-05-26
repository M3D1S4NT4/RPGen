package com.rpgen.core.action;

import com.rpgen.core.entity.Entity;
import java.util.Random;

public class PredefinedActions {
    public static class RockPaperScissorsAction extends AbstractGameAction {
        private static final Random random = new Random();
        private final String choice;

        public RockPaperScissorsAction(String id, String name, String choice) {
            super(id, name, 1, "Un ataque basado en " + choice + " que puede vencer o ser vencido");
            this.choice = choice.toLowerCase();
        }

        @Override
        public void execute(Entity source, Entity target) {
            if (!canExecute(source, target)) {
                return;
            }
            
            String targetChoice = getRandomChoice();
            int damage = calculateDamage(choice, targetChoice, source, target);
            
            if (damage > 0) {
                target.takeDamage(damage);
            } else if (damage < 0) {
                source.takeDamage(-damage);
            }
        }

        private String getRandomChoice() {
            String[] choices = {"piedra", "papel", "tijeras"};
            return choices[random.nextInt(choices.length)];
        }

        private int calculateDamage(String sourceChoice, String targetChoice, Entity source, Entity target) {
            if (sourceChoice.equals(targetChoice)) {
                return 0; // Empate
            }

            boolean sourceWins = (sourceChoice.equals("piedra") && targetChoice.equals("tijeras")) ||
                               (sourceChoice.equals("papel") && targetChoice.equals("piedra")) ||
                               (sourceChoice.equals("tijeras") && targetChoice.equals("papel"));

            return sourceWins ? source.getAttack() : -target.getAttack();
        }
    }

    public static GameAction createRockAction(String id) {
        return new RockPaperScissorsAction(id, "Piedra", "piedra");
    }

    public static GameAction createPaperAction(String id) {
        return new RockPaperScissorsAction(id, "Papel", "papel");
    }

    public static GameAction createScissorsAction(String id) {
        return new RockPaperScissorsAction(id, "Tijeras", "tijeras");
    }
} 