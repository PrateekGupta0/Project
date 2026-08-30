package com.tross.linkedinprofileapi.exception;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(String profileUrl) {
        super("No profile data found for URL: " + profileUrl);
    }
}
