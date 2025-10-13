package com.jm.services.ai;

import java.util.UUID;

public record AiResponse(String content, UUID requestId) {

    public static AiResponse withContent(String content) {
        return new AiResponse(content, null);
    }

    public static AiResponse withRequestId(UUID requestId) {
        return new AiResponse(null, requestId);
    }
}
