package com.tross.linkedinprofileapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Incoming request payload for POST /api/v1/profile.
 * The GET variant uses the same validation logic applied to a query param.
 */
public record ProfileRequest(

        @NotBlank(message = "profileUrl must not be blank")
        @Pattern(
                regexp = "^https?://([a-z]{2,3}\\.)?linkedin\\.com/in/[a-zA-Z0-9\\-_%]+/?$",
                message = "profileUrl must be a valid LinkedIn profile URL, e.g. https://www.linkedin.com/in/username"
        )
        String profileUrl

) {
}
