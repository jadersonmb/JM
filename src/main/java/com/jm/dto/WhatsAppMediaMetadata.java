package com.jm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsAppMediaMetadata {

    private String id;
    private String url;
    @JsonProperty("mime_type")
    private String mimeType;
    @JsonProperty("file_size")
    private Long fileSize;
    private String sha256;
}
