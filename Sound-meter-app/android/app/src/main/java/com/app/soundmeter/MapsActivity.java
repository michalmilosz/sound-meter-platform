package com.app.soundmeter;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.app.soundmeter.api.MeasurementAPI;
import com.app.soundmeter.configuration.DataHolder;
import com.app.soundmeter.configuration.RetrofitClient;
import com.app.soundmeter.dto.Measurement;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

// Mapa Google
@Slf4j
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Retrofit retrofit = RetrofitClient.getRetrofit();
    private MeasurementAPI measurementAPI = retrofit.create(MeasurementAPI.class);
    private List<Measurement> measurements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // metoda wykonywana kiedy mapa się załaduje w activity
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        // > 70 db
        Drawable badIconDrawable = getResources().getDrawable(R.drawable.bad_icon);
        BitmapDescriptor badIcon = getMarkerIconFromDrawable(badIconDrawable);

        // < 40db
        Drawable goodIconDrawable = getResources().getDrawable(R.drawable.good_icon);
        BitmapDescriptor goodIcon = getMarkerIconFromDrawable(goodIconDrawable);

        // <41, 70> db
        Drawable mediumIconDrawable = getResources().getDrawable(R.drawable.medium_icon);
        BitmapDescriptor mediumIcon = getMarkerIconFromDrawable(mediumIconDrawable);

        // zapytanie HTTP pobierające wszystkie pomiary usera oraz dodające do mapy wg. przyjętych wyzej kolorów
        Call<List<Measurement>> getAllMeasurements = measurementAPI.getMeasurements(DataHolder.getInstance().getUserProfile().getLogin());
        getAllMeasurements.enqueue(new Callback<List<Measurement>>() {
            @Override
            public void onResponse(Call<List<Measurement>> call, Response<List<Measurement>> response) {
                if (response.code() == 200) {
                    measurements = response.body();

                    if(!measurements.isEmpty()){
                        measurements
                                .stream()
                                .forEach(result -> {
                                    LatLng latLng = new LatLng(result.getGps_latitude(), result.getGps_longitude());

                                    if(result.getAvg() <= 40){
                                        mMap.addMarker(
                                                new MarkerOptions()
                                                        .position(latLng)
                                                        .icon(goodIcon)
                                                        .title(String.valueOf(result.getAvg() + " db"))
                                        );
                                    }
                                    else if(result.getAvg() > 40 && result.getAvg() <= 70){
                                        mMap.addMarker(
                                                new MarkerOptions()
                                                        .position(latLng)
                                                        .icon(mediumIcon)
                                                        .title(String.valueOf(result.getAvg() + " db"))
                                        );
                                    }
                                    else {
                                        mMap.addMarker(
                                                new MarkerOptions()
                                                        .position(latLng)
                                                        .icon(badIcon)
                                                        .title(String.valueOf(result.getAvg() + " db"))
                                        );
                                    }
                                });

                        LatLng baseLatLng = new LatLng(
                                measurements.get(measurements.size()-1).getGps_latitude(),
                                measurements.get(measurements.size()-1).getGps_longitude()
                        );
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(baseLatLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(7));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Measurement>> call, Throwable t) {
                log.info("/measurements/all (POST), Response: " + t.getMessage());
            }
        });
    }

    // tworzenie kolorowego punktu na podstawie drawable
    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}