package com.jm.execption;

import lombok.Getter;

@Getter
public enum ProblemType {

    INVALID_BODY("/invalid-body", "Invalid Body", "invalid_message_body"),
    INVALID_BODY_PARAM("/invalid-body", "Invaled Body", "invalid_message_body_param"),
    INVALID_VALUE_LONG_DATABASE("/invaled-long-database", "Value Long", "invalid_value_long_database"),

    USER_NOT_FOUND("/user-not-found", "User not found", "account_not_found"),
    IMAGE_NOT_FOUND("/image-not-found", "Image not found", "image_not_found"),
    PERSON_NOT_FOUND("/person-not-found", "Person not found", "person_not.found"),
    ANAMNESE_NOT_FOUND("/anamnese-not-found", "Anamnese not found", "anamnese.not-found"),
    CPF_ALREADY_FOUND("/cpf-found", "CPF already found", "cpf_already_found"),
    CELL_PHONE_ALREADY_found("/cellphone-found", "CellPhone already found", "cellphone_already_found"),
    DATE_INVALED("/date-invalid", "Date Invalid", "date_invalid"),

    ERROR_UPLOAD_FILE("/error-upload", "Error to uploading file", "error_upload_file"),
    ERROR_FACE_DETECTED("/error-face-detected", "Error to detected file", "error_detected_file"),
    ERROR_VIDEO_PROCESS("/error-video-process", "Error to process video file", "error_process_video_file"),
    ERROR_DOWNLOAD_FILE("/error-download", "Error to download file", "error_download_file"),
    ERROR_GET_FILE("/error-list", "Error to get file", "error_get_file"),
    ERROR_DELETE_FILE("/error-delete", "Error to delete file", "error_delete_file"),
    USER_NOT_CREATED("/user-not-created", "User not created", "user_not_created"),
    ERROR_GEMINI("/gemini-chat", "Error to chat with Gemini", "error_gemini_chat");

    private String uri;
    private String title;
    private String messageSource;

    ProblemType(String path, String title, String messageSource) {
        this.uri = "https://jm-project.com" + path;
        this.title = title;
        this.messageSource = messageSource;
    }
}
