package com.jm.email.templates;

import com.jm.configuration.config.EmailProperties;
import com.jm.email.EmailTemplateResolver;
import com.jm.email.EmailTemplateType;
import com.jm.email.TemplateEmailContent;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClasspathEmailTemplateResolver implements EmailTemplateResolver {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathEmailTemplateResolver.class);

    private final ResourceLoader resourceLoader;
    private final MessageSource messageSource;
    private final EmailProperties emailProperties;

    public ClasspathEmailTemplateResolver(ResourceLoader resourceLoader, MessageSource messageSource,
            EmailProperties emailProperties) {
        this.resourceLoader = resourceLoader;
        this.messageSource = messageSource;
        this.emailProperties = emailProperties;
    }

    @Override
    public TemplateEmailContent resolve(EmailTemplateType type, Locale locale, Map<String, Object> variables) {
        Set<String> candidateSuffixes = buildCandidates(locale);
        for (String suffix : candidateSuffixes) {
            String location = String.format("classpath:templates/email/%s_%s.txt", type.getTemplateName(), suffix);
            Resource resource = resourceLoader.getResource(location);
            if (resource.exists()) {
                return readAndRender(resource, variables);
            }
        }
        throw templateNotFound(type, locale, candidateSuffixes);
    }

    private Set<String> buildCandidates(Locale locale) {
        Set<String> candidates = new LinkedHashSet<>();
        if (locale != null) {
            String tag = locale.toLanguageTag();
            if (StringUtils.hasText(tag)) {
                candidates.add(tag);
            }
            if (StringUtils.hasText(locale.getLanguage())) {
                candidates.add(locale.getLanguage());
            }
        }
        if (StringUtils.hasText(emailProperties.getDefaultLanguage())) {
            Locale defaultLocale = Locale.forLanguageTag(emailProperties.getDefaultLanguage());
            String defaultTag = defaultLocale.toLanguageTag();
            if (StringUtils.hasText(defaultTag)) {
                candidates.add(defaultTag);
            }
            if (StringUtils.hasText(defaultLocale.getLanguage())) {
                candidates.add(defaultLocale.getLanguage());
            }
        }
        candidates.add("default");
        return candidates;
    }

    private TemplateEmailContent readAndRender(Resource resource, Map<String, Object> variables) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            if (!StringUtils.hasText(content)) {
                throw templateProcessingError(resource.getFilename(), "Template is empty");
            }
            int lineBreakIndex = content.indexOf('\n');
            String subjectLine = lineBreakIndex >= 0 ? content.substring(0, lineBreakIndex) : content;
            String body = lineBreakIndex >= 0 ? content.substring(lineBreakIndex + 1) : "";
            String subject = cleanPrefix(subjectLine);
            if (!CollectionUtils.isEmpty(variables)) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    String placeholder = String.format("{{%s}}", entry.getKey());
                    String value = entry.getValue() != null ? entry.getValue().toString() : "";
                    subject = subject.replace(placeholder, value);
                    body = body.replace(placeholder, value);
                }
            }
            return new TemplateEmailContent(subject.trim(), body.trim());
        } catch (IOException ex) {
            logger.error("Error reading email template {}", resource.getFilename(), ex);
            throw templateProcessingError(resource.getFilename(), ex.getMessage());
        }
    }

    private String cleanPrefix(String subjectLine) {
        if (!StringUtils.hasText(subjectLine)) {
            return "";
        }
        if (subjectLine.toLowerCase(Locale.ROOT).startsWith("subject:")) {
            return subjectLine.substring(subjectLine.indexOf(':') + 1).trim();
        }
        return subjectLine.trim();
    }

    private JMException templateNotFound(EmailTemplateType type, Locale locale, Set<String> candidates) {
        ProblemType problemType = ProblemType.EMAIL_TEMPLATE_NOT_FOUND;
        Locale messageLocale = LocaleContextHolder.getLocale();
        String details = messageSource.getMessage(problemType.getMessageSource(),
                new Object[] { type.getTemplateName(),
                        CollectionUtils.isEmpty(candidates) ? "" : String.join(", ", candidates),
                        locale != null ? locale.toLanguageTag() : "" },
                messageLocale);
        logger.error("Template {} not found for locale {}", type.getTemplateName(),
                locale != null ? locale.toLanguageTag() : "unknown");
        return new JMException(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemType.getTitle(),
                problemType.getUri(), details);
    }

    private JMException templateProcessingError(String templateName, String reason) {
        ProblemType problemType = ProblemType.EMAIL_TEMPLATE_PROCESSING_ERROR;
        Locale messageLocale = LocaleContextHolder.getLocale();
        String details = messageSource.getMessage(problemType.getMessageSource(),
                new Object[] { templateName, reason }, messageLocale);
        return new JMException(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemType.getTitle(),
                problemType.getUri(), details);
    }
}
