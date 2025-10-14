package com.jm.services;

import com.jm.dto.ImageDTO;
import com.jm.dto.UserDTO;
import com.jm.execption.JMException;
import com.jm.execption.ProblemType;
import com.jm.utils.ByteArrayMultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CloudflareR2Service {

    private static final Logger logger = LoggerFactory.getLogger(CloudflareR2Service.class);

    private final S3Client s3Client;
    private final ImageService imageService;
    private final UserService userService;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;
    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    public CloudflareR2Service(S3Client s3Client, ImageService imageService, UserService userService) {
        this.s3Client = s3Client;
        this.imageService = imageService;
        this.userService = userService;
    }

    public ImageDTO uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") UUID userId) {
        try {

            UserDTO userDTO = userService.findById(userId);

            logger.info("Starting to uploading file: {}", file.getOriginalFilename());
            String fileName = generateFileName(file.getOriginalFilename(), userDTO.getId());
            String key = (userDTO.getId() != null) ? userDTO.getId() + "/" + fileName : fileName;

            /* Faz o upload do file */
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(key)
                    .contentType(file.getContentType()).contentLength(file.getSize()).build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            ImageDTO imageDTO = imageService
                    .save(ImageDTO.builder()
                            .fileName(fileName)
                            .url(publicUrl + "/" + userDTO.getId() + "/" + fileName)
                            .userId(userDTO.getId())
                            .fileKey(key)
                            .build());

            imageDTO.setFileKey(key);
            return imageDTO;

        } catch (Exception e) {
            logger.info("Error to uploading file: {}", e.getMessage());
            ProblemType problemType = ProblemType.ERROR_UPLOAD_FILE;
            throw new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getUri(), problemType.getTitle(),
                    "Error to uploading file: " + e.getMessage());
        }

    }

    /* Upload de imagem com otimização */
    public ImageDTO uploadImage(MultipartFile imageFile, UUID userId) throws IOException {
        /* Aqui você pode adicionar compressão de imagem se necessário */
        return uploadFile(imageFile, userId);
    }

    public byte[] downloadFile(@PathVariable UUID userId, @PathVariable String fileName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName)
                    .key(userId + "/" + generateFileName(fileName, userId)).build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return objectBytes.asByteArray();
        } catch (Exception e) {
            logger.info("Error to download file: {}", e.getMessage());
            ProblemType problemType = ProblemType.ERROR_DOWNLOAD_FILE;
            throw new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getUri(), problemType.getTitle(),
                    "Error to download file: " + e.getMessage());
        }
    }

    public MultipartFile toMultipartFile(byte[] bytes, String filename, String contentType) {
        return new ByteArrayMultipartFile(bytes, "file", filename, contentType);
    }

    public List<String> listFiles() {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).prefix("").build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return response.contents().stream().map(S3Object::key).collect(Collectors.toList());
        } catch (Exception e) {
            logger.info("Error to get file: {}", e.getMessage());
            ProblemType problemType = ProblemType.ERROR_GET_FILE;
            throw new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getUri(), problemType.getTitle(),
                    "Error to get file: " + e.getMessage());
        }
    }

    public void deleteFile(UUID userId, String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName)
                    .key(userId + "/" + generateFileName(fileName, userId)).build();

            s3Client.deleteObject(deleteObjectRequest);
            imageService.delete(fileName);
        } catch (Exception e) {
            logger.info("Error to delete file: {}", e.getMessage());
            ProblemType problemType = ProblemType.ERROR_DELETE_FILE;
            throw new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getUri(), problemType.getTitle(),
                    "Error to delete file: " + e.getMessage());
        }
    }

    public void deleteFileByGeneratedName(UUID userId, String generatedFileName) {
        if (userId == null || !StringUtils.hasText(generatedFileName)) {
            return;
        }
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName)
                    .key(userId + "/" + generatedFileName).build();

            s3Client.deleteObject(deleteObjectRequest);
            imageService.delete(generatedFileName);
        } catch (Exception e) {
            logger.info("Error to delete file: {}", e.getMessage());
            ProblemType problemType = ProblemType.ERROR_DELETE_FILE;
            throw new JMException(HttpStatus.BAD_REQUEST.value(), problemType.getUri(), problemType.getTitle(),
                    "Error to delete file: " + e.getMessage());
        }
    }

    /*
     * Gerar URL pública (se configurado domínio no R2) Ou seu domínio customizado
     */
    public String getPublicUrl(String fileKey) {
        return "https://pub-" + fileKey + ".r2.dev";
    }

    private String generateFileName(String originalFileName, UUID userId) {
        return userId + "_" + originalFileName;
    }
}
