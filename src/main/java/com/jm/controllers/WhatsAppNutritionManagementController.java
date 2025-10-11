package com.jm.controllers;

import com.jm.dto.WhatsAppMessageFeedDTO;
import com.jm.dto.WhatsAppNutritionEntryRequest;
import com.jm.services.WhatsAppNutritionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/whatsapp")
@RequiredArgsConstructor
public class WhatsAppNutritionManagementController {

    private final WhatsAppNutritionService whatsappNutritionService;

    @PostMapping("/messages")
    public ResponseEntity<WhatsAppMessageFeedDTO> create(@Valid @RequestBody WhatsAppNutritionEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(whatsappNutritionService.createManualEntry(request));
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<WhatsAppMessageFeedDTO> update(@PathVariable UUID id,
            @Valid @RequestBody WhatsAppNutritionEntryRequest request) {
        return ResponseEntity.ok(whatsappNutritionService.updateEntry(id, request));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        whatsappNutritionService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }
}
