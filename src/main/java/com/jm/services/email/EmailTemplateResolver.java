package com.jm.email;

import java.util.Locale;
import java.util.Map;

public interface EmailTemplateResolver {

    TemplateEmailContent resolve(EmailTemplateType type, Locale locale, Map<String, Object> variables);
}
