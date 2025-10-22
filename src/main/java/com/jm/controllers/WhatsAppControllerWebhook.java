package com.jm.controllers;

import com.jm.services.WhatsAppNutritionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/public/api/v1/webhook/whatsapp")
@RequiredArgsConstructor
public class WhatsAppControllerWebhook {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppController.class);

    private final WhatsAppNutritionService whatsappNutritionService;
    @Value("${whatsapp.verify.token}")
    private String VERIFY_TOKEN;

    @PostMapping
    public ResponseEntity<Void> receiveMessage(@RequestBody Map<String, Object> payload) {
        logger.info("WhatsApp webhook received");
        whatsappNutritionService.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
            System.out.println("✅ Verificação bem-sucedida!");
            return ResponseEntity.ok(challenge);
        } else {
            System.out.println("❌ Verificação falhou!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
