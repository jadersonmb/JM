package com.jm.events;

import com.jm.entity.Users;
import com.jm.entity.WhatsAppMessage;
import com.jm.enums.AiProvider;
import java.util.Arrays;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NutritionAnalysisRequestEvent extends ApplicationEvent {

    private final WhatsAppMessage message;
    private final byte[] imageBytes;
    private final String mimeType;
    private final Users owner;
    private final String from;
    private final AiProvider provider;
    private final String model;

    public NutritionAnalysisRequestEvent(Object source, WhatsAppMessage message, byte[] imageBytes, String mimeType,
            Users owner, String from, AiProvider provider, String model) {
        super(source);
        this.message = message;
        this.imageBytes = imageBytes != null ? Arrays.copyOf(imageBytes, imageBytes.length) : null;
        this.mimeType = mimeType;
        this.owner = owner;
        this.from = from;
        this.provider = provider;
        this.model = model;
    }
}
