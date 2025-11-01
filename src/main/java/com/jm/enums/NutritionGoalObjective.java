package com.jm.enums;

public enum NutritionGoalObjective {

    MAINTENANCE(1.0, 1.9, "goal.ai.objective.maintenance"),
    WEIGHT_LOSS(0.85, 2.2, "goal.ai.objective.weight-loss"),
    MUSCLE_GAIN(1.10, 2.15, "goal.ai.objective.muscle-gain");

    private final double calorieMultiplier;
    private final double proteinPerKg;
    private final String messageKey;

    NutritionGoalObjective(double calorieMultiplier, double proteinPerKg, String messageKey) {
        this.calorieMultiplier = calorieMultiplier;
        this.proteinPerKg = proteinPerKg;
        this.messageKey = messageKey;
    }

    public double getCalorieMultiplier() {
        return calorieMultiplier;
    }

    public double getProteinPerKg() {
        return proteinPerKg;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
