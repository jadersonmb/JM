package com.jm.execption;

import lombok.Getter;

@Getter
public enum ProblemType {

    INVALID_BODY("/invalid-body", "Invalid Body", "invalid_message_body"),
    INVALID_BODY_PARAM("/invalid-body", "Invaled Body", "invalid_message_body_param"),
    INVALID_VALUE_LONG_DATABASE("/invaled-long-database", "Value Long", "invalid_value_long_database"),
    INVALID_DATA("/invalid-data", "Invalid Data", "invalid_data"),
    INVALID_TOKEN("/invalid-token", "Invalid token", "token.invalid"),
    EXPIRED_TOKEN("/expired-token", "Expired token", "token.expired"),
    INVALID_PASSWORD("/invalid-password", "Invalid password", "invalid_password"),

    USER_NOT_FOUND("/user-not-found", "User not found", "account_not_found"),
    IMAGE_NOT_FOUND("/image-not-found", "Image not found", "image_not_found"),
    PHOTO_EVOLUTION_NOT_FOUND("/photo-evolution-not-found", "Photo evolution not found", "photo.evolution.not-found"),
    PHOTO_EVOLUTION_FORBIDDEN("/photo-evolution-forbidden", "Photo evolution forbidden", "photo.evolution.forbidden"),
    PHOTO_EVOLUTION_INVALID_IMAGE("/photo-evolution-invalid-image", "Photo evolution image invalid",
            "photo.evolution.invalid-image"),
    PERSON_NOT_FOUND("/person-not-found", "Person not found", "person_not.found"),
    ANAMNESIS_NOT_FOUND("/anamnesis-not-found", "Anamnesis not found", "anamnesis.not-found"),
    COUNTRY_NOT_FOUND("/country-not-found", "Country not found", "country.not-found"),
    CITY_NOT_FOUND("/city-not-found", "City not found", "city.not-found"),
    EDUCATION_LEVEL_NOT_FOUND("/education-level-not-found", "Education level not found", "education-level.not-found"),
    PROFESSION_NOT_FOUND("/profession-not-found", "Profession not found", "profession.not-found"),
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
    ERROR_GEMINI("/gemini-chat", "Error to chat with Gemini", "error_gemini_chat"),
    DIET_NOT_FOUND("/diet-not-found", "Diet not found", "diet.not-found"),
    DIET_FORBIDDEN("/diet-forbidden", "Diet forbidden", "diet.forbidden"),
    DIET_INVALID_REFERENCE("/diet-invalid-reference", "Invalid diet reference", "diet.invalid-reference"),
    DIET_UNIT_NOT_FOUND("/diet-unit-not-found", "Unit of measure not found", "diet.unit.not-found"),
    DIET_FOOD_NOT_FOUND("/diet-food-not-found", "Food item not found", "diet.food.not-found"),
    NUTRITION_GOAL_NOT_FOUND("/nutrition-goal-not-found", "Nutrition goal not found", "goal.not-found"),
    NUTRITION_GOAL_FORBIDDEN("/nutrition-goal-forbidden", "Nutrition goal forbidden", "goal.forbidden"),
    NUTRITION_GOAL_TEMPLATE_NOT_FOUND("/nutrition-goal-template-not-found", "Nutrition goal template not found",
            "goal.template.not-found"),
    NUTRITION_GOAL_UNIT_NOT_FOUND("/nutrition-goal-unit-not-found", "Unit of measure not found",
            "goal.unit.not-found"),
    PATHOLOGY_NOT_FOUND("/pathology-not-found", "Pathology not found", "pathology.not-found"),
    EXERCISE_NOT_FOUND("/exercise-not-found", "Exercise not found", "exercise.not-found"),
    EXERCISE_REFERENCE_NOT_FOUND("/exercise-reference-not-found", "Exercise reference not found",
            "exercise.reference.not-found"),
    EXERCISE_FORBIDDEN("/exercise-forbidden", "Exercise forbidden", "exercise.forbidden"),
    ANALYTICS_FORBIDDEN("/analytics-forbidden", "Analytics access forbidden", "analytics.forbidden"),
    ANALYTICS_USER_NOT_FOUND("/analytics-user-not-found", "Analytics user not found", "analytics.user.not-found"),
    WHATSAPP_MESSAGE_NOT_FOUND("/whatsapp-message-not-found", "WhatsApp message not found",
            "whatsapp.message.not-found"),
    WHATSAPP_FORBIDDEN("/whatsapp-forbidden", "WhatsApp nutrition forbidden", "whatsapp.nutrition.forbidden"),
    REMINDER_NOT_FOUND("/reminder-not-found", "Reminder not found", "reminder.not-found"),
    REMINDER_FORBIDDEN("/reminder-forbidden", "Reminder forbidden", "reminder.forbidden"),
    EXTERNAL_SERVICE_ERROR("External Service Error", "external-service", "error.external.service"),
    EMAIL_TEMPLATE_NOT_FOUND("/email-template-not-found", "Email template not found", "email.template.not-found"),
    EMAIL_TEMPLATE_PROCESSING_ERROR("/email-template-processing-error", "Email template processing error",
            "email.template.processing-error"),
    EMAIL_SENDING_FAILED("/email-sending-failed", "Email sending failed", "email.sending.failed"),
    EMAIL_CONFIGURATION_INVALID("/email-configuration-invalid", "Email configuration invalid",
            "email.configuration.invalid"),
    EMAIL_INVALID_RECIPIENT("/email-invalid-recipient", "Email recipient invalid", "email.invalid.recipient");

    private String uri;
    private String title;
    private String messageSource;

    ProblemType(String path, String title, String messageSource) {
        this.uri = "https://jm-project.com" + path;
        this.title = title;
        this.messageSource = messageSource;
    }
}
