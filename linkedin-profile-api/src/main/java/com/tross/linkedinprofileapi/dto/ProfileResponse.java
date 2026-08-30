package com.tross.linkedinprofileapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponse {

    private String profileUrl;
    private String name;
    private String headline;
    private String location;
    private String about;
    private List<Experience> experience;
    private List<Education> education;
    private List<String> skills;
    private List<CertificationEntry> certifications;
    private List<LanguageEntry> languages;
    private List<String> profileImages;
    private SourceMetadata meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Experience {
        String title;
        String company;
        String employmentType;
        String dateRange;
        String location;
        String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Education{
        String institution;
        String degree;
        String fieldOfStudy;
        String dateRange;
        String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CertificationEntry {
        private String name;
        private String issuingOrganization;
        private String issueDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LanguageEntry {
        private String name;
        private String proficiency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SourceMetadata {
        private String source;
        private String retrievedAt;
        private boolean cached;
    }
}
