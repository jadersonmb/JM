package com.jm.configuration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class CloudflareR2Config {

    @Value("${cloudflare.r2.account-id}")
    private String accountId;
    @Value("${cloudflare.r2.access-key-id}")
    private String accessKeyId;
    @Value("${cloudflare.r2.secret-access-key}")
    private String secretAccessKey;
    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                ))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .checksumValidationEnabled(false)
                        .chunkedEncodingEnabled(true)
                        .build())
                .build();
    }
}