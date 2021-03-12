package com.app.soundmeter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.app.soundmeter.api.UserAPI;
import com.app.soundmeter.configuration.DataHolder;
import com.app.soundmeter.configuration.RetrofitClient;
import com.app.soundmeter.dto.ResponseMessage;
import com.app.soundmeter.dto.UserCredentials;
import com.app.soundmeter.dto.UserProfile;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

// Widok ustawien kalibracji dla zalogowanego usera
@Slf4j
public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.phoneModelEditText)
    EditText phoneModel;
    @BindView(R.id.userCalibrationEditText)
    EditText userCalibration;
    @BindView(R.id.settingsSaveButton)
    Button saveButton;

    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private UserAPI userAPI = retrofit.create(UserAPI.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        log.info(DataHolder.getInstance().getUserProfile().toString());
        userCalibration.setText(String.valueOf(DataHolder.getInstance().getUserProfile().getCalibration()));
        phoneModel.setText(DataHolder.getInstance().getUserProfile().getPhone());

        // reakcja na klikniecie buttona save
        saveButton.setOnClickListener(view -> {
            saveProfile(
                    UserProfile
                            .builder()
                            .calibration(Integer.valueOf(userCalibration.getText().toString()))
                            .phone(phoneModel.getText().toString())
                            .login(DataHolder.getInstance().getUserProfile().getLogin())
                            .build(),
                    view
            );
            finish();
        });
    }

    // update modelu telefonu oraz kalibracji - zapis do bazy po HTTP
    public void saveProfile(UserProfile userProfile, final View v) {
        Call<ResponseMessage> save = userAPI.saveProfile(userProfile);
        save.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                log.info("/measurements/save (POST), Response: " + response.code() + "/ message: " + response.message());
                if (response.code() == 200) {
                    DataHolder.getInstance().getUserProfile().setCalibration(Integer.valueOf(userCalibration.getText().toString()));
                    DataHolder.getInstance().getUserProfile().setPhone(phoneModel.getText().toString());
                } else {
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                log.info("/measurements/save (POST), Response: " + t.getMessage());
            }
        });
    }
}