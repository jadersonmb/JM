package com.jm.controllers;

import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.security.annotation.PermissionRequired;
import com.jm.services.CloudflareR2Service;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLConnection;
import java.util.UUID;

@RestController
@Data
@AllArgsConstructor
@RequestMapping("/api/v1/cloudflare/storage")
@Tag(name = "Cloudflare Storage", description = "Operations with Cloudflare R2 storage")
@SecurityRequirement(name = "BearerAuth")
public class CloudFlareStorageController {

    private final CloudflareR2Service r2Service;

    @PermissionRequired("ROLE_CLOUD_STORAGE_CREATE")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) UUID userId) {
        return ResponseEntity.ok(r2Service.uploadFile(file, userId));
    }

    @PermissionRequired("ROLE_CLOUD_STORAGE_READ")
    @GetMapping("/download/{userId}/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID userId, @PathVariable String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + userId + "\"")
                .body(r2Service.downloadFile(userId, fileName));
    }

    @PermissionRequired("ROLE_CLOUD_STORAGE_READ")
    @GetMapping("/view/{userId}/{fileName}")
    public ResponseEntity<byte[]> viewImage(@PathVariable UUID userId, @PathVariable String fileName) {
            byte[] imageData = r2Service.downloadFile(userId, fileName);
            String contentType = URLConnection.guessContentTypeFromName(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageData);
    }

    @PermissionRequired("ROLE_CLOUD_STORAGE_READ")
    @GetMapping
    public ResponseEntity<?> listFiles() {
        return ResponseEntity.ok().body(r2Service.listFiles());
    }

    @PermissionRequired("ROLE_CLOUD_STORAGE_DELETE")
    @DeleteMapping("/{userId}/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable UUID userId, @PathVariable String fileName) {
        r2Service.deleteFile(userId, fileName);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({JMException.class})
    public ResponseEntity<Object> Exception(JMException ex) {
        Problem problem = createProblemBuild(ex.getStatus(), ex.getDetails(), ex.getType(), ex.getTitle())
                .build();
        return ResponseEntity.badRequest().body(problem);
    }

    private Problem.ProblemBuilder createProblemBuild(Integer status, String detail, String type, String title) {
        return Problem.builder()
                .status(status)
                .details(detail)
                .type(type)
                .title(title);
    }
}