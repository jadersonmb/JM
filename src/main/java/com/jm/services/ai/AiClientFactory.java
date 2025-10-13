package com.jm.services.ai;

import com.jm.enums.AiProvider;

public interface AiClientFactory {

    AiClient createClient(AiProvider provider);
}
