package com.jm.services.email;

public enum EmailTemplateType {

    USER_CONFIRMATION("user-confirmation"),
    WELCOME("welcome"),
    PASSWORD_RECOVERY("password-recovery");

    private final String templateName;

    EmailTemplateType(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}
