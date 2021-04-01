package com.app.soundmeter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.app.soundmeter.configuration.DataHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuActivity extends AppCompatActivity {

    @BindView(R.id.ButtonMeter)
    Button buttonMeter;
    @BindView(R.id.ButtonMaps)
    Button buttonMaps;
    @BindView(R.id.ButtonSettings)
    Button buttonSettings;
    @BindView(R.id.TextViewLogout)
    TextView textViewLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);

        textViewLogout.setOnClickListener(view -> {
            openLoginActivity();
        });

        buttonMeter.setOnClickListener(view -> {
            startActivity(new Intent(this, UserMeasurement.class));
        });

        buttonMaps.setOnClickListener(view -> {
            openMapsActivity();
        });

        buttonSettings.setOnClickListener(view -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    public void openLoginActivity() {
        DataHolder.getInstance().getUserProfile().setLogin("");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void openMapsActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}