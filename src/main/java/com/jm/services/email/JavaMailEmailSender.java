package com.jm.email;

import com.jm.configuration.config.EmailProperties;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class JavaMailEmailSender implements EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(JavaMailEmailSender.class);

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;
    private final MessageSource messageSource;

    public JavaMailEmailSender(JavaMailSender mailSender, EmailProperties emailProperties,
            MessageSource messageSource) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
        this.messageSource = messageSource;
    }

    @Override
    public void send(EmailMessage message) {
        if (message == null || !StringUtils.hasText(message.to())) {
            throw invalidRecipient();
        }
        if (!StringUtils.hasText(emailProperties.getFrom())) {
            throw invalidConfiguration();
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            helper.setFrom(emailProperties.getFrom());
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.body(), false);
            mailSender.send(mimeMessage);
            logger.info("Email sent to {} with subject {}", message.to(), message.subject());
        } catch (MailException | MessagingException ex) {
            logger.error("Error sending email to {}", message.to(), ex);
            throw sendingFailed(ex);
        }
    }

    private JMException sendingFailed(Exception ex) {
        ProblemType problemType = ProblemType.EMAIL_SENDING_FAILED;
        Locale locale = LocaleContextHolder.getLocale();
        String details = messageSource.getMessage(problemType.getMessageSource(), new Object[] { ex.getMessage() },
                locale);
        return new JMException(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemType.getTitle(),
                problemType.getUri(), details);
    }

    private JMException invalidConfiguration() {
        ProblemType problemType = ProblemType.EMAIL_CONFIGURATION_INVALID;
        Locale locale = LocaleContextHolder.getLocale();
        String details = messageSource.getMessage(problemType.getMessageSource(), null, locale);
        return new JMException(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemType.getTitle(),
                problemType.getUri(), details);
    }

    private JMException invalidRecipient() {
        ProblemType problemType = ProblemType.EMAIL_INVALID_RECIPIENT;
        Locale locale = LocaleContextHolder.getLocale();
        String details = messageSource.getMessage(problemType.getMessageSource(), null, locale);
        return new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getTitle(), problemType.getUri(), details);
    }
}
