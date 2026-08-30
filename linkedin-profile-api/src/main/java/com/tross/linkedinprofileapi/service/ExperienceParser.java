package com.tross.linkedinprofileapi.service;

import com.tross.linkedinprofileapi.dto.ProfileResponse;

import java.util.List;
import java.util.Locale;

public class ExperienceParser
        implements SectionParser<ProfileResponse.Experience> {

    @Override
    public boolean supports(String componentId) {
        return componentId
                .toLowerCase(Locale.ROOT)
                .contains("experience");
    }

    @Override
    public List<ProfileResponse.Experience> parse(String response) {

        // Parse captured RSC response here

        return List.of();
    }
}
