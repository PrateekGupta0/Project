package com.tross.linkedinprofileapi.service;


import java.util.*;
import java.util.regex.*;

public class LinkedInComponentExtractor {

    public record ComponentRequest(
            String componentId,
            String vanityName,
            String parentSpanId
    ) {}

    private static final String COMPONENT_PREFIX =
            "com.linkedin.sdui.generated.profile.dsl.impl.";

    private static final Pattern VANITY_PATTERN =
            Pattern.compile(
                    "\\\\?\"vanityName\\\\?\"\\s*:\\s*\\\\?\"([^\"]+)\\\\?\""
            );

    /*
     * Matches:
     *
     * "componentKey":"some-parent-id"
     *
     * ...
     *
     * "newComponentId":"com.linkedin.sdui.generated.profile.dsl.impl.someComponent"
     *
     * The [^{}]* restriction keeps the match inside the same object level
     * for the response structure we are processing.
     */
    private static final Pattern COMPONENT_REQUEST_PATTERN =
            Pattern.compile(
                    "\\\\?\"\\$type\\\\?\"\\s*:\\s*\\\\?\"proto\\.sdui\\.actions\\.core\\.ReplaceComponent\\\\?\""
                            + "[\\s\\S]*?"
                            + "\\\\?\"componentKey\\\\?\"\\s*:\\s*\\\\?\"([^\"]+)\\\\?\""
                            + "[\\s\\S]*?"
                            + "\\\\?\"newComponentId\\\\?\"\\s*:\\s*\\\\?\"("
                            + Pattern.quote(COMPONENT_PREFIX)
                            + "[^\"]+)\\\\?\""
            );


    private static final String EXPERIENCE_ID = "com.linkedin.sdui.generated.profile.dsl.impl.profileCardsExperienceOnly";
    private static final String NOT_EXPERIENCE_ID = "com.linkedin.sdui.generated.profile.dsl.impl.profileCardsBelowActivityPart1WithoutExp";

    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile( "\\\\?\"componentKey\\\\?\"\\s*:\\s*\\\\?\"([^\"]+)\\\\?\"" + "\\\\?\"content\\\\?\"\\s*:\\s*\\{" + "\\s*\\\\?\"\\$case\\\\?\"\\s*:\\s*\\\\?\"asyncContent\\\\?\"" + "[\\s\\S]{0,1000}?" + "\\\\?\"newComponentId\\\\?\"\\s*:\\s*\\\\?\"" + Pattern.quote(EXPERIENCE_ID) + "\\\\?\"" );
    private static final Pattern NOT_EXPERIENCE_PATTERN = Pattern.compile( "\\\\?\"componentKey\\\\?\"\\s*:\\s*\\\\?\"([^\"]+)\\\\?\"" + "\\\\?\"content\\\\?\"\\s*:\\s*\\{" + "\\s*\\\\?\"\\$case\\\\?\"\\s*:\\s*\\\\?\"asyncContent\\\\?\"" + "[\\s\\S]{0,1000}?" + "\\\\?\"newComponentId\\\\?\"\\s*:\\s*\\\\?\"" + Pattern.quote(NOT_EXPERIENCE_ID) + "\\\\?\"" );

    public static List<ComponentRequest> findComponents(String html) {

        String vanityName = null;
        Matcher vanityMatcher = VANITY_PATTERN.matcher(html);
        if (vanityMatcher.find()) {
            vanityName = vanityMatcher.group(1);
        }
        List<ComponentRequest> result = new ArrayList<>(2);
        Matcher experienceMatcher = EXPERIENCE_PATTERN.matcher(html);
        while (experienceMatcher.find()) {
            result.add( new ComponentRequest( "com.linkedin.sdui.generated.profile.dsl.impl.profileCardsExperienceOnly", vanityName, experienceMatcher.group(1) ) );
        }
        Matcher notExperienceMatcher = NOT_EXPERIENCE_PATTERN.matcher(html);
        while (notExperienceMatcher.find()) {
            result.add( new ComponentRequest( "com.linkedin.sdui.generated.profile.dsl.impl.profileCardsBelowActivityPart1WithoutExp", vanityName, notExperienceMatcher.group(1) ) );
        }
        return result;
    }


}


