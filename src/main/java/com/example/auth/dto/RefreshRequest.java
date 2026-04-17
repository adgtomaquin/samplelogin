// RefreshRequest.java
package com.example.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RefreshRequest {

    @NotBlank
    @JsonProperty("refresh_token")
    private String refreshToken;
}