package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppMessageDTO {

    private String phoneNumber;
    private String message;
    private List<Image> image;
    private UUID userId;

    @Data
    public static class Image {
        private String link;
        private String caption;
        private String mediaId;
    }
}
