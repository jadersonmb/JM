package com.jm.services.ai;

import com.jm.enums.AiProvider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DefaultAiClientFactory implements AiClientFactory {

    private final Map<AiProvider, AiClient> registry;

    public DefaultAiClientFactory(List<AiClient> clients) {
        this.registry = clients == null ? new EnumMap<>(AiProvider.class)
                : clients.stream().filter(Objects::nonNull)
                        .collect(Collectors.toMap(AiClient::provider, Function.identity(), (a, b) -> a,
                                () -> new EnumMap<>(AiProvider.class)));
    }

    @Override
    public AiClient createClient(AiProvider provider) {
        AiClient client = registry.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("No AI client registered for provider " + provider);
        }
        return client;
    }
}
