package com.app.soundmeter.configuration;

import com.app.soundmeter.dto.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Klasa statyczna do przechowywania wartości dostępnych w całej aplikacji
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataHolder {

    private static DataHolder instance = new DataHolder();

    public static DataHolder getInstance(){
        return instance;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    private UserProfile userProfile;

}
