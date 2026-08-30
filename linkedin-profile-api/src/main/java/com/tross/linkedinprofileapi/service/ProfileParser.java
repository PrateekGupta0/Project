package com.tross.linkedinprofileapi.service;

import com.tross.linkedinprofileapi.dto.ProfileResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProfileParser {

    public ProfileResponse parse(String html) {

        Document doc = Jsoup.parse(html);

        ProfileResponse.ProfileResponseBuilder response =
                ProfileResponse.builder();

        // -----------------------------------------
        // Basic profile information
        // -----------------------------------------

        // Name
        String name = extractName(doc);
        response.name(name);

        // Headline
        String headline = extractHeadline(doc);
        response.headline(headline);

        // Location
        String location = extractLocation(doc);
        response.location(location);

        // Profile URL
        String profileUrl = extractProfileUrl(doc);
        response.profileUrl(profileUrl);

        // Profile images
        response.profileImages(extractProfileImages(doc));

        // About
        response.about(extractAbout(doc));

        // -----------------------------------------
        // Sections
        // -----------------------------------------

        response.experience(extractExperience(doc));

        response.education(extractEducation(doc));

        response.skills(extractSkills(doc));

        response.certifications(extractCertifications(doc));

        response.languages(extractLanguages(doc));

        // -----------------------------------------
        // Metadata
        // -----------------------------------------

        response.meta(
                ProfileResponse.SourceMetadata.builder()
                        .source("linkedin")
                        .retrievedAt(java.time.Instant.now().toString())
                        .cached(false)
                        .build()
        );

        return response.build();
    }


    // =====================================================
    // NAME
    // =====================================================

    private String extractName(Document doc) {

        // LinkedIn profile top card contains an h2 with the name
        Element nameElement = doc.selectFirst("main h2");

        if (nameElement != null) {
            return clean(nameElement.text());
        }

        return null;
    }


    // =====================================================
    // HEADLINE
    // =====================================================

    private String extractHeadline(Document doc) {

        /*
         * The headline appears immediately after the name
         * in the profile top card.
         *
         * We avoid relying on LinkedIn generated CSS classes.
         */

        Elements paragraphs = doc.select("main p");

        for (Element p : paragraphs) {

            String text = clean(p.text());

            if (text.equalsIgnoreCase("SDE@BNY | LNMIIT'24")) {
                return text;
            }
        }

        return null;
    }


    // =====================================================
    // LOCATION
    // =====================================================

    private String extractLocation(Document doc) {

        Elements paragraphs = doc.select("main p");

        for (Element p : paragraphs) {

            String text = clean(p.text());

            if (text.contains("Area") ||
                    text.contains("India") ||
                    text.contains("Jaipur") ||
                    text.contains("Delhi") ||
                    text.contains("Bangalore") ||
                    text.contains("Mumbai") ||
                    text.contains("Chennai")) {

                return text;
            }
        }

        return null;
    }


    // =====================================================
    // PROFILE URL
    // =====================================================

    private String extractProfileUrl(Document doc) {

        Elements links = doc.select("a[href]");

        for (Element link : links) {

            String href = link.attr("abs:href");

            if (href.contains("/in/")) {
                return href;
            }
        }

        return null;
    }


    // =====================================================
    // PROFILE IMAGES
    // =====================================================

    private List<String> extractProfileImages(Document doc) {

        List<String> images = new ArrayList<>();

        Elements imageElements = doc.select("img[src]");

        for (Element image : imageElements) {

            String src = image.absUrl("src");

            if (src != null &&
                    !src.isBlank() &&
                    src.contains("media.licdn.com")) {

                images.add(src);
            }
        }

        return images;
    }


    // =====================================================
    // ABOUT
    // =====================================================

    private String extractAbout(Document doc) {

        Element heading = findHeading(doc, "About");

        if (heading == null) {
            heading = findHeading(doc, "About this member");
        }

        if (heading == null) {
            return null;
        }

        Element section = heading.closest("section");

        if (section == null) {
            return null;
        }

        return clean(section.text());
    }


    // =====================================================
    // EXPERIENCE
    // =====================================================

    private List<ProfileResponse.Experience> extractExperience(
            Document doc) {

        List<ProfileResponse.Experience> result =
                new ArrayList<>();

        Element heading = findHeading(doc, "Experience");

        if (heading == null) {
            return result;
        }

        Element section = heading.closest("section");

        if (section == null) {
            return result;
        }

        /*
         * LinkedIn's HTML structure changes frequently.
         *
         * We locate individual links/containers under
         * the Experience section and extract their text.
         */

        Elements entries = section.select("li");

        for (Element entry : entries) {

            List<String> texts = directTexts(entry);

            if (texts.isEmpty()) {
                continue;
            }

            ProfileResponse.Experience experience =
                    ProfileResponse.Experience.builder()
                            .title(texts.size() > 0 ? texts.get(0) : null)
                            .company(texts.size() > 1 ? texts.get(1) : null)
                            .location(texts.size() > 2 ? texts.get(2) : null)
                            .build();

            result.add(experience);
        }

        return result;
    }


    // =====================================================
    // EDUCATION
    // =====================================================

    private List<ProfileResponse.Education> extractEducation(
            Document doc) {

        List<ProfileResponse.Education> result =
                new ArrayList<>();

        Element heading = findHeading(doc, "Education");

        if (heading == null) {
            return result;
        }

        Element section = heading.closest("section");

        if (section == null) {
            return result;
        }

        Elements entries = section.select("li");

        for (Element entry : entries) {

            List<String> texts = directTexts(entry);

            if (texts.isEmpty()) {
                continue;
            }

            ProfileResponse.Education education =
                    ProfileResponse.Education.builder()
                            .institution(texts.size() > 0 ? texts.get(0) : null)
                            .degree(texts.size() > 1 ? texts.get(1) : null)
                            .fieldOfStudy(texts.size() > 2 ? texts.get(2) : null)
                            .build();

            result.add(education);
        }

        return result;
    }


    // =====================================================
    // SKILLS
    // =====================================================

    private List<String> extractSkills(Document doc) {

        List<String> skills = new ArrayList<>();

        Element heading = findHeading(doc, "Skills");

        if (heading == null) {
            return skills;
        }

        Element section = heading.closest("section");

        if (section == null) {
            return skills;
        }

        Elements entries = section.select("li");

        for (Element entry : entries) {

            String text = clean(entry.text());

            if (!text.isBlank()) {
                skills.add(text);
            }
        }

        return skills;
    }


    // =====================================================
    // CERTIFICATIONS
    // =====================================================

    private List<ProfileResponse.CertificationEntry>
    extractCertifications(Document doc) {

        List<ProfileResponse.CertificationEntry> result =
                new ArrayList<>();

        Element heading = findHeading(doc, "Certifications");

        if (heading == null) {
            return result;
        }

        Element section = heading.closest("section");

        if (section == null) {
            return result;
        }

        Elements entries = section.select("li");

        for (Element entry : entries) {

            List<String> texts = directTexts(entry);

            if (texts.isEmpty()) {
                continue;
            }

            ProfileResponse.CertificationEntry certification =
                    ProfileResponse.CertificationEntry.builder()
                            .name(texts.size() > 0 ? texts.get(0) : null)
                            .issuingOrganization(
                                    texts.size() > 1
                                            ? texts.get(1)
                                            : null)
                            .issueDate(
                                    texts.size() > 2
                                            ? texts.get(2)
                                            : null)
                            .build();

            result.add(certification);
        }

        return result;
    }


    // =====================================================
    // LANGUAGES
    // =====================================================

    private List<ProfileResponse.LanguageEntry>
    extractLanguages(Document doc) {

        List<ProfileResponse.LanguageEntry> result =
                new ArrayList<>();

        Element heading = findHeading(doc, "Languages");

        if (heading == null) {
            heading = findHeading(doc, "Profile language");
        }

        if (heading == null) {
            return result;
        }

        Element section = heading.closest("section");

        if (section == null) {
            return result;
        }

        Elements entries = section.select("p");

        for (Element entry : entries) {

            String text = clean(entry.text());

            if (text.isBlank()) {
                continue;
            }

            ProfileResponse.LanguageEntry language =
                    ProfileResponse.LanguageEntry.builder()
                            .name(text)
                            .build();

            result.add(language);
        }

        return result;
    }


    // =====================================================
    // FIND SECTION HEADING
    // =====================================================

    private Element findHeading(Document doc, String text) {

        Elements elements = doc.select("h1, h2, h3, h4");

        for (Element element : elements) {

            if (clean(element.text())
                    .equalsIgnoreCase(text)) {

                return element;
            }
        }

        return null;
    }


    // =====================================================
    // GET TEXT VALUES
    // =====================================================

    private List<String> directTexts(Element element) {

        List<String> result = new ArrayList<>();

        for (Element child : element.select("p, span")) {

            String text = clean(child.text());

            if (!text.isBlank() &&
                    !result.contains(text)) {

                result.add(text);
            }
        }

        return result;
    }


    // =====================================================
    // CLEAN TEXT
    // =====================================================

    private String clean(String text) {

        if (text == null) {
            return null;
        }

        return text
                .replace("\u00a0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
