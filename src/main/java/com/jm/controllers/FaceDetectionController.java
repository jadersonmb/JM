package com.jm.controllers;

import com.jm.execption.JMException;
import com.jm.execption.Problem;
import com.jm.services.FaceDetectionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facial")
@Data
@AllArgsConstructor
public class FaceDetectionController {

    private final FaceDetectionService faceDetectionService;

    @PostMapping("/detect")
    public ResponseEntity<?> detectFaces(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(faceDetectionService.detectFacesOpenCV(file));
    }

    @PostMapping("/recognize/{userId}")
    public ResponseEntity<?> recognizeFace(@RequestParam("file") MultipartFile file, @PathVariable UUID userId) {
        return ResponseEntity.ok(faceDetectionService.recognizeFaceFromVideo(file, userId));
    }
    @PostMapping("/recognize/image")
    public ResponseEntity<?> recognizeFaceImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(faceDetectionService.recognizeImage(file));
    }

    @PostMapping("/train/model/{userId}")
    public ResponseEntity<?> trainModel(@PathVariable UUID userId) throws Exception {
        return ResponseEntity.ok(faceDetectionService.trainModelByUserId(userId));
    }

    @PostMapping("/train/model/byVideo/{userId}")
    public ResponseEntity<?> trainModelByVideoAndUser(@RequestParam("file") MultipartFile file, @PathVariable UUID userId) throws Exception {
        return ResponseEntity.ok(faceDetectionService.processVideoTrainByUser(file,userId));
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
