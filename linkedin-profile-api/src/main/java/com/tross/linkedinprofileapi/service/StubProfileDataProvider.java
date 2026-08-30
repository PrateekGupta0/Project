package com.tross.linkedinprofileapi.service;

import com.tross.linkedinprofileapi.dto.ProfileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class StubProfileDataProvider implements ProfileDataProvider {


    RestTemplate restTemplate=new RestTemplate();

    @Autowired
    private ProfileParser profileParser;
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("linkedin\\.com/in/([a-zA-Z0-9\\-_%]+)/?");

    @Override
    public ProfileResponse fetchProfile(String profileUrl,String cookie) {
        String username = extractUsername(profileUrl);
        String displayName = toDisplayName(username);
        System.out.println("Cookie: "+cookie);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Forward cookie
        if (cookie != null && !cookie.isBlank()) {
            headers.set(HttpHeaders.COOKIE, cookie);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Call downstream GET API
        ResponseEntity<String> response = restTemplate.exchange(
                profileUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        String html = response.getBody();

        List<LinkedInComponentExtractor.ComponentRequest> components =
                LinkedInComponentExtractor.findComponents(html);

        List<String> componentResponse=new ArrayList<>();
        for(LinkedInComponentExtractor.ComponentRequest request:components){
            String compId=request.componentId().toLowerCase();
            if(compId.contains("experience"))
                componentResponse.add(getComponent(request.componentId(),request.parentSpanId(), request.vanityName(),cookie));
        }

        for(String res:componentResponse){
            System.out.println(res);
        }

        ProfileResponse profileResponse=profileParser.parse(response.getBody());
        return profileResponse;
    }



    private String extractUsername(String profileUrl) {
        Matcher m = USERNAME_PATTERN.matcher(profileUrl);
        return m.find() ? m.group(1) : "unknown";
    }

    private String toDisplayName(String username) {
        String cleaned = username.replaceAll("[-_]", " ").replaceAll("\\d", "").trim();
        if (cleaned.isBlank()) {
            return "Sample User";
        }
        String[] parts = cleaned.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isBlank()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }


    public String getComponent(
            String componentId,
            String parentSpanId,
            String vanityName,
            String cookie
    ) {

        String url =
                "https://www.linkedin.com/flagship-web/rsc-action/actions/component"
                        + "?componentId=" + componentId
                        + "&sduiid=" + componentId
                        + "&parentSpanId=" + parentSpanId;

        String requestBody = """
            {
                "clientArguments": {
                    "payload": {
                        "isSelfView": false,
                        "vanityName": "%s",
                        "replaceableSectionArgs": {
                            "vanityName": "%s",
                            "hideCardsForGoldenGate": false,
                            "shouldSetupReplaceableComponent": true,
                            "isSelfView": false,
                            "isSelfViewResolved": false
                        }
                    },
                    "states": [],
                    "screenId": "com.linkedin.sdui.flagshipnav.profile.Profile",
                    "knownTemplateIds": []
                }
            }
            """.formatted(vanityName, vanityName);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "*/*");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setOrigin("https://www.linkedin.com");
        headers.add("Cookie",cookie);

        HttpEntity<String> requestEntity =
                new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            requestEntity,
                            String.class
                    );

            System.out.println("HTTP Status: " + response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(
                        "Request failed: "
                                + response.getStatusCode()
                                + "\n"
                                + response.getBody()
                );
            }


            return response.getBody();
        }
        catch (Exception e){
            e.printStackTrace();

        }

        return "";
    }
}
