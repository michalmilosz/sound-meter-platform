package com.app.soundmeter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.app.soundmeter.api.UserAPI;
import com.app.soundmeter.configuration.DataHolder;
import com.app.soundmeter.configuration.RetrofitClient;
import com.app.soundmeter.dto.ResponseMessage;
import com.app.soundmeter.dto.UserCredentials;
import com.app.soundmeter.dto.UserProfile;
import com.google.android.gms.common.util.ScopeUtil;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@Slf4j
public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.phoneModelEditText)
    EditText phoneModel;
    @BindView(R.id.userSettingsMinTension)
    EditText minTension;
    @BindView(R.id.userSettingsMaxTension)
    EditText maxTension;
    @BindView(R.id.userSettingsStartButton)
    Button startButton;
    @BindView(R.id.userSettingsMinDbValue)
    EditText minDb;
    @BindView(R.id.userSettingsMaxDbValue)
    EditText maxDb;
    @BindView(R.id.userSettingsStopButton)
    Button stopButton;
    @BindView(R.id.userSettingsSaveButton)
    Button saveButton;

    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private UserAPI userAPI = retrofit.create(UserAPI.class);

    private MediaRecorder mediaRecorder = null;
    private Timer timer;
    private static List<Integer> results;
    private boolean toAdd = false;
    private Handler handler = new Handler();
    private Integer maxDecibelsValue, minDecibelsValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        log.info(DataHolder.getInstance().getUserProfile().toString());
        phoneModel.setText(DataHolder.getInstance().getUserProfile().getPhone());

        stopButton.setEnabled(false);

        if(DataHolder.getInstance().getUserProfile() != null) {
            minTension.setText(Integer.toString(DataHolder.getInstance().getUserProfile().getMin_v()));
            maxTension.setText(Integer.toString(DataHolder.getInstance().getUserProfile().getMax_v()));
            minDb.setText(String.valueOf(DataHolder.getInstance().getUserProfile().getMin_db()));
            maxDb.setText(String.valueOf(DataHolder.getInstance().getUserProfile().getMax_db()));
        }

        if(checkPermissions())
            startRecording();
        else
            requirePermission();

        startButton.setOnClickListener(view -> {
            minTension.getText().clear();
            maxTension.getText().clear();
            minDb.getText().clear();
            maxDb.getText().clear();
            results = new ArrayList<>();
            toAdd = true;
            stopButton.setEnabled(true);
            startButton.setEnabled(false);
        });

        stopButton.setOnClickListener(view -> {
            toAdd = false;
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
            maxDecibelsValue = Collections.max(results);
            minDecibelsValue = Collections.min(results);
            minTension.setText(String.valueOf(minDecibelsValue));
            maxTension.setText(String.valueOf(maxDecibelsValue));
        });

        saveButton.setOnClickListener(view -> {
            if(minTension.getText().toString().isEmpty())
                minTension.setText(String.valueOf(0));
            if(maxTension.getText().toString().isEmpty())
                maxTension.setText(String.valueOf(0));
            if(minDb.getText().toString().isEmpty())
                minDb.setText(String.valueOf(0));
            if(maxDb.getText().toString().isEmpty())
                maxDb.setText(String.valueOf(0));

            saveProfile(
                    UserProfile
                            .builder()
                            .phone(phoneModel.getText().toString())
                            .login(DataHolder.getInstance().getUserProfile().getLogin())
                            .min_v(Integer.parseInt(String.valueOf(minTension.getText())))
                            .max_v(Integer.parseInt(String.valueOf(maxTension.getText())))
                            .min_db(Float.parseFloat(String.valueOf(minDb.getText())))
                            .max_db(Float.parseFloat(String.valueOf(maxDb.getText())))
                            .build()
            );
            finish();
        });
    }

    public void saveProfile(UserProfile userProfile) {
        Call<ResponseMessage> save = userAPI.saveProfile(userProfile);
        save.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                log.info("/users/login (POST), Response: " + response.code() + "/ message: " + response.message());
                if (response.code() == 200) {
                    DataHolder.getInstance().getUserProfile().setPhone(phoneModel.getText().toString());
                    DataHolder.getInstance().getUserProfile().setMin_v(Integer.parseInt(minTension.getText().toString()));
                    DataHolder.getInstance().getUserProfile().setMax_v(Integer.parseInt(maxTension.getText().toString()));
                    DataHolder.getInstance().getUserProfile().setMin_db(Float.parseFloat(String.valueOf(minDb.getText())));
                    DataHolder.getInstance().getUserProfile().setMax_db(Float.parseFloat(String.valueOf(maxDb.getText())));
                } else {
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                log.info("/users/login (POST), Response: " + t.getMessage());
            }
        });
    }

    private void startRecording(){
        if (mediaRecorder == null)
        {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            timer = new Timer();
            timer.scheduleAtFixedRate(new RecorderTask(mediaRecorder), 0, 100);
            mediaRecorder.setOutputFile("/data/data/com.app.soundmeter/test.3gp");
            try
            {
                mediaRecorder.prepare();
            }
            catch (Exception e) {
                log.info("", e.getStackTrace());
            }
            mediaRecorder.start();
        }
    }

    private boolean checkPermissions(){
        int write_external_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage == PackageManager.PERMISSION_GRANTED &&
                record_audio == PackageManager.PERMISSION_GRANTED;
    }

    private void requirePermission(){
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, 100);
    }

    private class RecorderTask extends TimerTask {
        TextView sound = (TextView) findViewById(R.id.settingsDecibelsTextView);
        private MediaRecorder recorder;

        public RecorderTask(MediaRecorder recorder) {
            this.recorder = recorder;
        }

        public void run() {
            runOnUiThread((Runnable) () -> {
                if(mediaRecorder!=null) {
                    int amplitude = recorder.getMaxAmplitude();
                    sound.setText(Integer.toString(amplitude));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (toAdd) {
                                results.add(amplitude);
                            }
                        }
                    });
                }
            });
        }
    }

    private void stopRecording(){
        timer.cancel();
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
        timer = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRecording();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRecording();
    }
}