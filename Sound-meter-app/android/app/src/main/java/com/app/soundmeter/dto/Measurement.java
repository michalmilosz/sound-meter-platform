package com.app.soundmeter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Measurement {

    private float min;

    private float max;

    private float avg;

    private float gps_longitude;

    private float gps_latitude;

    private String login;
}
