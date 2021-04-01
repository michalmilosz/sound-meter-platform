package com.app.soundmeter.configuration;

import com.app.soundmeter.dto.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataHolder {

    private static DataHolder instance = new DataHolder();

    public static DataHolder getInstance(){
        return instance;
    }

    private UserProfile userProfile;

}
