package com.example.fixmyarea.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fixmyarea.BuildConfig;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Cloudinary image uploader using REST API
 */
public class CloudinaryUploader {

    private static final String TAG = "CloudinaryUploader";

    // Cloudinary Configuration - Loaded from BuildConfig (secure)
    private static final String CLOUD_NAME = BuildConfig.CLOUDINARY_CLOUD_NAME;
    private static final String UPLOAD_PRESET = BuildConfig.CLOUDINARY_UPLOAD_PRESET;

    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    /**
     * Callback interface for upload progress
     */
    public interface UploadCallback {
        void onSuccess(String imageUrl);

        void onFailure(String error);

        void onProgress(int progress);
    }

    /**
     * Upload image to Cloudinary
     * 
     * @param context  Application context
     * @param imageUri URI of the image to upload
     * @param callback Upload callback
     */
    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                callback.onProgress(10);

                // Read image file
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    callback.onFailure("Failed to read image");
                    return;
                }

                byte[] imageBytes = new byte[inputStream.available()];
                inputStream.read(imageBytes);
                inputStream.close();

                callback.onProgress(30);

                // Create multipart request
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "image.jpg",
                                RequestBody.create(imageBytes, MediaType.parse("image/*")))
                        .addFormDataPart("upload_preset", UPLOAD_PRESET)
                        .addFormDataPart("folder", "profile_images")
                        .build();

                Request request = new Request.Builder()
                        .url(UPLOAD_URL)
                        .post(requestBody)
                        .build();

                callback.onProgress(50);

                // Execute request
                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                callback.onProgress(90);

                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);
                        String secureUrl = json.getString("secure_url");

                        Log.d(TAG, "Upload successful: " + secureUrl);
                        callback.onProgress(100);
                        callback.onSuccess(secureUrl);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse response", e);
                        callback.onFailure("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    String errorMsg = "Upload failed: " + response.code() + " - " + response.message();
                    Log.e(TAG, errorMsg);
                    callback.onFailure(errorMsg);
                }

                response.close();

            } catch (Exception e) {
                Log.e(TAG, "Error uploading image", e);
                callback.onFailure("Upload error: " + e.getMessage());
            }
        }).start();
    }
}
