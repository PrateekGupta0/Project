package com.tross.linkedinprofileapi.service;

import com.tross.linkedinprofileapi.dto.ProfileResponse;

import java.util.List;
import java.util.Locale;

public class EducationParser
        implements SectionParser<ProfileResponse.Education> {

    @Override
    public boolean supports(String componentId) {
        return componentId
                .toLowerCase(Locale.ROOT)
                .contains("education");
    }

    @Override
    public List<ProfileResponse.Education> parse(String response) {

        // Parse captured RSC response here

        return List.of();
    }
}
