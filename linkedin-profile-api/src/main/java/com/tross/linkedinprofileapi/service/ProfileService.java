package com.tross.linkedinprofileapi.service;

import com.tross.linkedinprofileapi.dto.ProfileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProfileService {


    private final ProfileDataProvider provider;

    public ProfileService(ProfileDataProvider profileDataProvider) {
        this.provider=profileDataProvider;
    }

    public ProfileResponse getProfile(String profileUrl,String cookie){
        return provider.fetchProfile(profileUrl,cookie);
    }
    public String normalize(String url) {
        String noFragment = url.split("#")[0];
        String noQuery = noFragment.split("\\?")[0];
        return noQuery.endsWith("/") ? noQuery.substring(0, noQuery.length() - 1) : noQuery;
    }
}
