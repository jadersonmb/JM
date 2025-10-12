package com.jm.events;

import com.jm.entity.Ollama;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OllamaRequestEvent extends ApplicationEvent {
    private final Ollama ollama;

    public OllamaRequestEvent(Object source, Ollama ollama) {
        super(source);
        this.ollama = ollama;
    }
}
