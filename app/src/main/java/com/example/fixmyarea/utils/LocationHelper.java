package com.example.fixmyarea.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for location-related operations
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";

    /**
     * Check if location permissions are granted
     */
    public static boolean hasLocationPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if location services are enabled on the device
     */
    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Convert coordinates to address (Reverse Geocoding)
     * 
     * @param context   Application context
     * @param latitude  Latitude coordinate
     * @param longitude Longitude coordinate
     * @param callback  Callback to receive the address result
     */
    public static void getAddressFromCoordinates(Context context, double latitude, double longitude,
            GeocodingCallback callback) {
        if (!Geocoder.isPresent()) {
            callback.onFailure("Geocoder not available");
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ - Use async API
                    geocoder.getFromLocation(latitude, longitude, 1, addresses -> {
                        if (addresses != null && !addresses.isEmpty()) {
                            callback.onSuccess(formatAddress(addresses.get(0)));
                        } else {
                            callback.onFailure("No address found");
                        }
                    });
                } else {
                    // Below Android 13 - Use sync API
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        callback.onSuccess(formatAddress(addresses.get(0)));
                    } else {
                        callback.onFailure("No address found");
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding error", e);
                callback.onFailure("Geocoding failed: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Format address from Address object
     */
    private static String formatAddress(Address address) {
        StringBuilder addressBuilder = new StringBuilder();

        // Add street address
        String featureName = address.getFeatureName();
        if (featureName != null && !featureName.isEmpty()) {
            addressBuilder.append(featureName).append(", ");
        }

        // Add locality (city)
        String locality = address.getLocality();
        if (locality != null && !locality.isEmpty()) {
            addressBuilder.append(locality).append(", ");
        }

        // Add admin area (state/province)
        String adminArea = address.getAdminArea();
        if (adminArea != null && !adminArea.isEmpty()) {
            addressBuilder.append(adminArea).append(", ");
        }

        // Add country
        String country = address.getCountryName();
        if (country != null && !country.isEmpty()) {
            addressBuilder.append(country);
        }

        String result = addressBuilder.toString();

        // Clean up trailing comma and space
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }

        // If nothing was found, use the full address lines
        if (result.isEmpty() && address.getMaxAddressLineIndex() >= 0) {
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressBuilder.append(address.getAddressLine(i));
                if (i < address.getMaxAddressLineIndex()) {
                    addressBuilder.append(", ");
                }
            }
            result = addressBuilder.toString();
        }

        return result.isEmpty() ? "Unknown location" : result;
    }

    /**
     * Get coordinates from address (Forward Geocoding)
     * 
     * @param context       Application context
     * @param addressString Address string to geocode
     * @param callback      Callback to receive the coordinates result
     */
    public static void getCoordinatesFromAddress(Context context, String addressString,
            CoordinatesCallback callback) {
        if (!Geocoder.isPresent()) {
            callback.onFailure("Geocoder not available");
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ - Use async API
                    geocoder.getFromLocationName(addressString, 1, addresses -> {
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            callback.onSuccess(address.getLatitude(), address.getLongitude());
                        } else {
                            callback.onFailure("No coordinates found");
                        }
                    });
                } else {
                    // Below Android 13 - Use sync API
                    List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        callback.onSuccess(address.getLatitude(), address.getLongitude());
                    } else {
                        callback.onFailure("No coordinates found");
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding error", e);
                callback.onFailure("Geocoding failed: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Callback interface for geocoding results (coordinates to address)
     */
    public interface GeocodingCallback {
        void onSuccess(String address);

        void onFailure(String error);
    }

    /**
     * Callback interface for reverse geocoding results (address to coordinates)
     */
    public interface CoordinatesCallback {
        void onSuccess(double latitude, double longitude);

        void onFailure(String error);
    }
}
