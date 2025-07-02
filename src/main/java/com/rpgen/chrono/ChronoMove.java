package com.rpgen.chrono;

import java.util.List;

public class ChronoMove {
    private String name;
    private String type;
    private int power;
    private int cost;
    private String target;
    private String moveType; // single, double, triple
    private List<String> requiredCharacters;
    private String description;
    private String owner;

    public ChronoMove(String name, String type, int power, int cost, String target) {
        this.name = name;
        this.type = type;
        this.power = power;
        this.cost = cost;
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getPower() {
        return power;
    }

    public int getCost() {
        return cost;
    }

    public String getTarget() {
        return target;
    }

    public void setType(String moveType) { this.moveType = moveType; }
    public String getMoveType() { return moveType; }
    public void setRequiredCharacters(List<String> chars) { this.requiredCharacters = chars; }
    public List<String> getRequiredCharacters() { return requiredCharacters; }
    public void setDescription(String desc) { this.description = desc; }
    public String getDescription() { return description; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getOwner() { return owner; }
} 