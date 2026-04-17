// ValidationErrorResponse.java
package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ValidationErrorResponse {

    private String error;
    private String message;
    private List<FieldError> details;

    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String issue;
    }
}