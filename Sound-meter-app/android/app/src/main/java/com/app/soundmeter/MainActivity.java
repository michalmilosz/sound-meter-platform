package com.app.soundmeter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.extern.slf4j.Slf4j;

// Wykonywanie pomiaru dla niezalogowanego usera
@Slf4j
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.stopButton)
    Button stopButton;
    @BindView(R.id.startButton)
    Button startButton;
    @BindView(R.id.maxDecibels)
    TextView maxDecibels;
    @BindView(R.id.minDecibels)
    TextView minDecibels;
    @BindView(R.id.avgDecibels)
    TextView avgDecibels;
    @BindView(R.id.calibrationEditText)
    EditText calibrationEditText;

    final int REQUEST_PERMISSION_CODE = 1000;
    private MediaRecorder mediaRecorder = null;
    private Timer timer;
    private static List<Double> results;
    private boolean toAdd = false;
    private Handler handler = new Handler();
    private double maxDecibelsValue, minDecibelsValue, avgDecibelsValue;
    private int calibration = 0;
    private boolean isStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if(checkPermissions())
            startRecording();
        else
            requirePermission();

        stopButton.setEnabled(false);

        // rozpoczecie pomiaru
        startButton.setOnClickListener(view -> {
            if(!calibrationEditText.getText().toString().isEmpty())
                calibration = Integer.parseInt(calibrationEditText.getText().toString());
            else
                calibration = 0;
            results = new ArrayList<>();
            toAdd = true;
            stopButton.setEnabled(true);
            startButton.setEnabled(false);
        });

        // koniec pomiaru
        stopButton.setOnClickListener(view -> {
            toAdd = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            results = results.
                    stream()
                    .map(result ->
                            result + calibration)
                    .collect(Collectors.toList());
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

    // start pomiaru
    private void startRecording(){
        if (mediaRecorder == null)
        {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            timer = new Timer();
            timer.scheduleAtFixedRate(new RecorderTask(mediaRecorder), 0, 100);
            mediaRecorder.setOutputFile("/dev/null");
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

    // zatrzymanie pomiaru
    private void stopRecording(){
            timer.cancel();
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            timer = null;
    }

    // sprawdzenie uprawnien do mikrofonu
    private boolean checkPermissions(){
        int write_external_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage == PackageManager.PERMISSION_GRANTED &&
                record_audio == PackageManager.PERMISSION_GRANTED;
    }

    // nadanie uprawnien
    private void requirePermission(){
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }

    // wykonywanie pomiaru w wątkach
    private class RecorderTask extends TimerTask {
        TextView sound = (TextView) findViewById(R.id.decibelsTextView);
        private MediaRecorder recorder;

        public RecorderTask(MediaRecorder recorder) {
            this.recorder = recorder;
        }

        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                }
            });
        }
    }
}

