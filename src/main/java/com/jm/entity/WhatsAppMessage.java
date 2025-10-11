package com.jm.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "whatsapp_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMessage {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy"))
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "whatsapp_message_id")
    private String whatsappMessageId;

    @Column(name = "from_phone")
    private String fromPhone;

    @Column(name = "to_phone")
    private String toPhone;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "media_id")
    private String mediaId;

    @Column(name = "media_url", columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "cloudflare_image_url")
    private String cloudflareImageUrl;

    @ManyToOne
    @JoinColumn(name = "owner_user_id")
    private Users owner;

    @Column(name = "manual_entry", nullable = false)
    private boolean manualEntry;

    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private NutritionAnalysis nutritionAnalysis;

    @jakarta.persistence.PrePersist
    public void prePersist() {
        if (receivedAt == null) {
            receivedAt = OffsetDateTime.now();
        }
    }
}
