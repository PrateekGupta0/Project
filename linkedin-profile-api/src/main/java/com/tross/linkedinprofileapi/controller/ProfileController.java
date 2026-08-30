package com.tross.linkedinprofileapi.controller;

import com.tross.linkedinprofileapi.dto.ProfileRequest;
import com.tross.linkedinprofileapi.dto.ProfileResponse;
import com.tross.linkedinprofileapi.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@Validated
@Tag(name = "Profile", description = "Fetch structured profile data for a public profile URL")
public class ProfileController {

    private static final String URL_PATTERN =
            "^https?://([a-z]{2,3}\\.)?linkedin\\.com/in/[a-zA-Z0-9\\-_%]+/?$";

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "Get profile data by URL (query param)")
    @ApiResponse(responseCode = "200", description = "Profile data returned successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or malformed profile URL")
    @ApiResponse(responseCode = "404", description = "No data found for the given profile")
    @ApiResponse(responseCode = "502", description = "Upstream data provider failed")
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @Parameter(description = "Public profile URL", example = "https://www.linkedin.com/in/example-user")
            @RequestParam
            @NotBlank
            @Pattern(regexp = URL_PATTERN, message = "profileUrl must be a valid LinkedIn profile URL")
            String url,HttpServletRequest req) {

        String normalized = profileService.normalize(url);
        String cookie=req.getHeader("Cookie");
        ProfileResponse response = profileService.getProfile(normalized,cookie);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get profile data by URL (JSON body)")
    @ApiResponse(responseCode = "200", description = "Profile data returned successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or malformed profile URL")
    @ApiResponse(responseCode = "404", description = "No data found for the given profile")
    @ApiResponse(responseCode = "502", description = "Upstream data provider failed")
    @PostMapping
    public ResponseEntity<ProfileResponse> postProfile(@Valid @RequestBody ProfileRequest request, HttpServletRequest req) {
        String normalized = profileService.normalize(request.profileUrl());
        String cookie=req.getHeader("Cookie");
        ProfileResponse response = profileService.getProfile(normalized,cookie);
        return ResponseEntity.ok(response);
    }
}
