package com.jm.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public enum WhatsAppCaptionTemplate {

    DAILY_EN("daily_en", "daily_en", "en_US",
            List.of("meal_name", "portion", "dish_name", "dish_emoji", "protein", "carbs", "fat", "fiber", "kcal",
                    "protein_total", "carb_total", "fat_total", "fiber_total", "kcal_total"),
            "\uD83C\uDF73 {{meal_name}}: {{portion}}g {{dish_name}} {{dish_emoji}} ({{protein}} g proteína | {{carbs}} g carboidrato | {{fat}} g gordura | {{fiber}} g fibras | {{kcal}} kcal) \uD83C\uDFAF Total: \uD83E\uDD57 Proteínas: {{protein_total}} g \uD83C\uDF5E Carboidratos: {{carb_total}} g \uD83C\uDF73 Gorduras: {{fat_total}} g \uD83C\uDF97\uFE0F Fibras: {{fiber_total}} g \uD83C\uDF7D\uFE0F Ingeridas: {{kcal_total}} kcal"),
    DAILY_SUMMARY_EN("daily_sumary_en", "daily_sumary_en", "en_US",
            List.of("date", "protein_total", "carb_total", "fat_total", "fiber_total", "water_total", "kcal_total",
                    "kcal_goal", "deficit", "status"),
            "\uD83D\uDDD3\uFE0F Daily summary - {{date}} \uD83E\uDD57 Protein: {{protein_total}} g \uD83C\uDF5E Carbs: {{carb_total}} g \uD83C\uDF73 Fat: {{fat_total}} g \uD83C\uDF97\uFE0F Fiber: {{fiber_total}} g \uD83D\uDEB0 Water: {{water_total}} ml \uD83D\uDD25 Calories consumed: {{kcal_total}} kcal \uD83C\uDFAF Goal: {{kcal_goal}} kcal Result: {{deficit}} kcal ({{status}})"),
    EDIT_MEALS_EN("edit_meals_en", "edit_meals_en", "en_US",
            List.of("meal_name", "portion", "dish_name", "dish_emoji", "protein", "carbs", "fat", "fiber", "kcal",
                    "protein_total", "carb_total", "fat_total", "fiber_total", "water_total", "kcal_total",
                    "kcal_total_1", "tmb", "deficit"),
            "\u2705 Changes saved successfully! \uD83C\uDF5D Meal: {{meal_name}} \uD83C\uDF73 {{portion}}g {{dish_name}} {{dish_emoji}} ({{protein}} g protein | {{carbs}} g carbs | {{fat}} g fat | {{fiber}} g fiber | {{kcal}} kcal) \uD83C\uDFAF Daily total: \uD83E\uDD57 Protein: {{protein_total}} g \uD83C\uDF5E Carbs: {{carb_total}} g \uD83C\uDF73 Fat: {{fat_total}} g \uD83C\uDF97\uFE0F Fiber: {{fiber_total}} g \uD83D\uDEB0 Water: {{water_total}} ml \uD83D\uDD25 Calories consumed: {{kcal_total}} kcal Deficit: {{kcal_total_1}} - {{tmb}} = {{deficit}} kcal"),
    MEAL_REMINDERS_EN("meal_reminders_en", "meal_reminders_en", "en_US",
            List.of("user_name", "meal_name", "dish_name", "kcal", "protein"),
            "\u23F0 Time for your next meal, {{user_name}}! \uD83C\uDF74 {{meal_name}} is ready \uD83D\uDCAA Suggestion: {{dish_name}} ({{kcal}} kcal | {{protein}} g protein) Don't skip meals — consistency is key! \u26A1"),
    GOLS_WATER_PT("gols_water_pt", "gols_water_pt", "pt_BR", List.of("user_name", "water_goal", "water_current",
            "water_remaining"),
            "\uD83D\uDCA7 Time to drink some water, {{user_name}}! Daily goal: {{water_goal}} ml You've already had {{water_current}} ml. Only {{water_remaining}} ml left! \uD83D\uDEB0");

    private final String code;
    private final String templateName;
    private final String languageCode;
    private final List<String> parameterOrder;
    private final String template;

    WhatsAppCaptionTemplate(String code, String templateName, String languageCode, List<String> parameterOrder,
            String template) {
        this.code = code;
        this.templateName = templateName;
        this.languageCode = languageCode;
        this.parameterOrder = parameterOrder == null ? Collections.emptyList() : List.copyOf(parameterOrder);
        this.template = template;
    }

    public String code() {
        return code;
    }

    public String templateName() {
        return templateName;
    }

    public String languageCode() {
        return languageCode;
    }

    public List<String> parameterOrder() {
        return parameterOrder;
    }

    public String format(Map<String, ?> variables) {
        String formatted = template;
        if (variables != null && !variables.isEmpty()) {
            for (Map.Entry<String, ?> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey().toLowerCase(Locale.ROOT) + "}}";
                Object value = entry.getValue();
                String replacement = value == null ? "" : Objects.toString(value);
                formatted = formatted.replace(placeholder, replacement);
            }
        }
        return formatted.replaceAll("\\{\\{[^}]+}}", "").trim();
    }

    public Map<String, Object> buildTemplatePayload(String to, Map<String, ?> variables) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", to);
        payload.put("type", "template");

        Map<String, Object> template = new java.util.HashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", languageCode));

        List<Map<String, Object>> components = buildComponents(variables);
        if (!components.isEmpty()) {
            template.put("components", components);
        }

        payload.put("template", template);
        return payload;
    }

    private List<Map<String, Object>> buildComponents(Map<String, ?> variables) {
        if (parameterOrder.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> parameters = new ArrayList<>();
        for (String key : parameterOrder) {
            Object value = variables != null ? variables.get(key) : null;
            String text = value == null ? "" : Objects.toString(value);
            parameters.add(Map.of("type", "text", "text", text));
        }
        return List.of(Map.of("type", "body", "parameters", parameters));
    }
}
