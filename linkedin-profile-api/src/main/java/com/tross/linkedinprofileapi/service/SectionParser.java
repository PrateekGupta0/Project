package com.tross.linkedinprofileapi.service;

import java.util.List;

public interface SectionParser<T> {
    boolean supports(String componentId);

    List<T> parse(String response);
}
