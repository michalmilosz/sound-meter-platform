package com.app.soundmeter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {

    private String phone;

    private String login;

    private Integer min_v;

    private Integer max_v;

    private float min_db;

    private float max_db;
}
