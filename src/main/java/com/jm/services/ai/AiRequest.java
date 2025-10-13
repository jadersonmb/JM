package com.jm.services.ai;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record AiRequest(
        AiRequestType type,
        String model,
        String prompt,
        Boolean stream,
        UUID userId,
        String from,
        List<MultipartFile> attachments,
        byte[] imageBytes,
        String mimeType,
        Duration timeout) {

    public boolean streamEnabled() {
        return Boolean.TRUE.equals(stream);
    }
}
