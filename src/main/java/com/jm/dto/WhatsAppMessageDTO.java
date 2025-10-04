package com.jm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.google.auto.value.AutoValue.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppMessageDTO {

    private String phoneNumber;
    private String message;
    private List<Image> image;

    @Data
    public static class Image {
        private String link;
        private String caption;
        private String mediaId;
    }
}
