package com.tross.linkedinprofileapi.service;

import com.tross.linkedinprofileapi.dto.ProfileResponse;


public interface ProfileDataProvider {
    ProfileResponse fetchProfile(String profileUrl,String cookie);
}
