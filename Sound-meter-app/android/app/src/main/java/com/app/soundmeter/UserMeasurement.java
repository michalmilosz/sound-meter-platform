package com.app.soundmeter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.soundmeter.api.MeasurementAPI;
import com.app.soundmeter.api.UserAPI;
import com.app.soundmeter.configuration.DataHolder;
import com.app.soundmeter.configuration.RetrofitClient;
import com.app.soundmeter.dto.Measurement;
import com.app.soundmeter.dto.ResponseMessage;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

// Pomiar dla uzytkownika
@Slf4j
public class UserMeasurement extends AppCompatActivity {

    @BindView(R.id.saveToDatabase)
    Button saveToDatabase;
    @BindView(R.id.umStartButton)
    Button startButton;
    @BindView(R.id.umStopButton)
    Button stopButton;
    @BindView(R.id.umMaxDecibels)
    TextView maxDecibels;
    @BindView(R.id.umMinDecibels)
    TextView minDecibels;
    @BindView(R.id.umAvgDecibels)
    TextView avgDecibels;

    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private MeasurementAPI measurementAPI = retrofit.create(MeasurementAPI.class);

    private double latitude;
    private double longitude;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private MediaRecorder mediaRecorder = null;
    private Timer timer;
    private static List<Double> results;
    private boolean toAdd = false;
    private Handler handler = new Handler();
    private double maxDecibelsValue, minDecibelsValue, avgDecibelsValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_measurement);
        ButterKnife.bind(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                UserMeasurement.this
        );

        stopButton.setEnabled(false);

        // sprawdzenie uprawnien
        if(ActivityCompat.checkSelfPermission(UserMeasurement.this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(UserMeasurement.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(UserMeasurement.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
        if(checkPermissions())
            startRecording();
        else
            requirePermission();
        startButton.setOnClickListener(view -> {
            results = new ArrayList<>();
            toAdd = true;
            stopButton.setEnabled(true);
            startButton.setEnabled(false);
        });

        // zastopowanie pomiaru
        stopButton.setOnClickListener(view -> {
            toAdd = false;
            results = results.
                    stream()
                    .map(result ->
                            result + DataHolder.getInstance().getUserProfile().getCalibration())
                    .collect(Collectors.toList());
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
            maxDecibelsValue = Collections.max(results);
            minDecibelsValue = Collections.min(results);
            avgDecibelsValue =
                    results.
                            stream()
                            .mapToDouble(result->result)
                            .average()
                            .orElse(0);
            maxDecibels.setText(String.format("Max: %.2f dB", maxDecibelsValue));
            minDecibels.setText(String.format("Min: %.2f dB", minDecibelsValue));
            avgDecibels.setText(String.format("Średnia: %.2f dB", avgDecibelsValue));
        });

        // klikniecie zapisz
        saveToDatabase.setOnClickListener(view -> {
            sendToDatabase(
                    Measurement.
                            builder()
                            .min((float) minDecibelsValue)
                            .max((float) maxDecibelsValue)
                            .avg((float) avgDecibelsValue)
                            .gps_latitude((float) latitude)
                            .gps_longitude((float) longitude)
                            .login(DataHolder.getInstance().getUserProfile().getLogin())
                            .build()
            );
        });
    }

    // Sprawdzenie uprawnien do GPS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && grantResults.length>0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
            getCurrentLocation();
        } else {
            Toast.makeText(getApplicationContext(), "Permission denid", Toast.LENGTH_SHORT).show();
        }
    }

    // Pobieranie wartosci GPS
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location!=null){
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    LocationRequest locationRequest = new LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1);
                    LocationCallback locationCallback = new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            Location location1 = locationResult.getLastLocation();
                            latitude = location1.getLatitude();
                            longitude = location1.getLongitude();
                        }
                    };
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                            locationCallback, Looper.myLooper());
                }
            });
        } else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    // rozpoczecie pomiaru
    private void startRecording(){
        if (mediaRecorder == null)
        {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            timer = new Timer();
            timer.scheduleAtFixedRate(new RecorderTask(mediaRecorder), 0, 100);
            //MediaRecorder.setOutputFile("/dev/null");
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

    // sprawdzenie uprawnien do mikrofonu
    private boolean checkPermissions(){
        int write_external_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage == PackageManager.PERMISSION_GRANTED &&
                record_audio == PackageManager.PERMISSION_GRANTED;
    }

    // ustawienie uprawnien
    private void requirePermission(){
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, 100);
    }

    // wykonywanie pomiaru w wątkach
    private class RecorderTask extends TimerTask {
        TextView sound = (TextView) findViewById(R.id.umDecibelsTextView);
        private MediaRecorder recorder;

        public RecorderTask(MediaRecorder recorder) {
            this.recorder = recorder;
        }

        public void run() {
            runOnUiThread((Runnable) () -> {
                if(mediaRecorder!=null) {
                    int amplitude = recorder.getMaxAmplitude();
                    double amplitudeDb = 20 * Math.log10((double) Math.abs(amplitude));
                    sound.setText(String.format("%.2f", amplitudeDb));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (toAdd) {
                                results.add(amplitudeDb);
                            }
                        }
                    });
                }
            });
        }
    }

    // zatrzymanie pomiaru
    private void stopRecording(){
        timer.cancel();
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
        timer = null;
    }

    // zatrzymanie pomiaru w wątku
    @Override
    protected void onPause() {
        super.onPause();
        stopRecording();
    }

    // start pomiaru w wątku
    @Override
    protected void onResume() {
        super.onResume();
        startRecording();
    }

    // Zapis pomiaru do bazy danych
    private void sendToDatabase(Measurement measurement){
        Call<ResponseMessage> measurementCall = measurementAPI.saveMeasurement(measurement);
        measurementCall.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                if (response.code() == 201) {
                    Toast.makeText(getApplicationContext(), "Zapisano do bazy", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                log.info("/measurements/save (POST), Response: " + t.getMessage());
            }
        });
    }
}