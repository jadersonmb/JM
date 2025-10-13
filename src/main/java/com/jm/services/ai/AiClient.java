package com.jm.services.ai;

import com.jm.enums.AiProvider;

public interface AiClient {

    AiProvider provider();

    AiResponse execute(AiRequest request);
}
