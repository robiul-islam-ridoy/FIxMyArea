package com.example.fixmyarea.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.fixmyarea.R;
import com.example.fixmyarea.utils.LocationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Activity for picking location on OpenStreetMap
 */
public class LocationPickerActivity extends AppCompatActivity implements MapListener {

    private static final String TAG = "LocationPickerActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final double DEFAULT_LATITUDE = 23.8103; // Dhaka, Bangladesh
    private static final double DEFAULT_LONGITUDE = 90.4125;
    private static final double DEFAULT_ZOOM = 15.0;

    // UI Components
    private MapView mapView;
    private TextView addressTextView;
    private ExtendedFloatingActionButton confirmButton;
    private ProgressBar progressBar;
    private ImageButton backButton;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = DEFAULT_LATITUDE;
    private double currentLongitude = DEFAULT_LONGITUDE;
    private String currentAddress = "";

    // Handler for debouncing geocoding requests
    private Handler geocodingHandler = new Handler(Looper.getMainLooper());
    private Runnable geocodingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure osmdroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_location_picker);

        // Initialize views
        initializeViews();

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup map
        setupMap();

        // Setup listeners
        setupListeners();

        // Request location permission and get current location
        checkLocationPermissionAndGetLocation();
    }

    private void initializeViews() {
        mapView = findViewById(R.id.mapView);
        addressTextView = findViewById(R.id.addressTextView);
        confirmButton = findViewById(R.id.confirmButton);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backButton);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);

        // Set default position
        IMapController mapController = mapView.getController();
        mapController.setZoom(DEFAULT_ZOOM);
        mapController.setCenter(new GeoPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE));

        // Add map listener for scroll events
        mapView.addMapListener(this);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        confirmButton.setOnClickListener(v -> {
            if (currentAddress != null && !currentAddress.isEmpty()) {
                returnLocation();
            } else {
                Toast.makeText(this, "Please wait while loading address...",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationPermissionAndGetLocation() {
        if (!LocationHelper.hasLocationPermissions(this)) {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (!LocationHelper.hasLocationPermissions(this)) {
            showPermissionDeniedDialog();
            return;
        }

        if (!LocationHelper.isLocationEnabled(this)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        progressBar.setVisibility(View.GONE);

                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

                            // Center map on current location
                            IMapController mapController = mapView.getController();
                            mapController.setCenter(new GeoPoint(currentLatitude, currentLongitude));

                            // Get address for current location
                            updateAddress(currentLatitude, currentLongitude);

                            Log.d(TAG, "Current location: " + currentLatitude + ", " + currentLongitude);
                        } else {
                            Toast.makeText(this, "Unable to get current location. Using default.",
                                    Toast.LENGTH_SHORT).show();
                            updateAddress(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Failed to get location", e);
                        Toast.makeText(this, "Failed to get current location",
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Location permission error", e);
        }
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        // Get center of the map when user scrolls
        GeoPoint center = (GeoPoint) mapView.getMapCenter();
        currentLatitude = center.getLatitude();
        currentLongitude = center.getLongitude();

        // Debounce geocoding to avoid too many requests
        if (geocodingRunnable != null) {
            geocodingHandler.removeCallbacks(geocodingRunnable);
        }

        geocodingRunnable = () -> updateAddress(currentLatitude, currentLongitude);
        geocodingHandler.postDelayed(geocodingRunnable, 500); // 500ms delay

        return true;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        // No action needed on zoom
        return true;
    }

    private void updateAddress(double latitude, double longitude) {
        addressTextView.setText("Loading address...");

        LocationHelper.getAddressFromCoordinates(this, latitude, longitude,
                new LocationHelper.GeocodingCallback() {
                    @Override
                    public void onSuccess(String address) {
                        runOnUiThread(() -> {
                            currentAddress = address;
                            addressTextView.setText(address);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            currentAddress = String.format("Lat: %.6f, Lng: %.6f", latitude, longitude);
                            addressTextView.setText(currentAddress);
                            Log.e(TAG, "Geocoding failed: " + error);
                        });
                    }
                });
    }

    private void returnLocation() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", currentLatitude);
        resultIntent.putExtra("longitude", currentLongitude);
        resultIntent.putExtra("address", currentAddress);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage(
                        "This feature requires location permission to get your current location and show it on the map.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (geocodingHandler != null && geocodingRunnable != null) {
            geocodingHandler.removeCallbacks(geocodingRunnable);
        }
    }
}
