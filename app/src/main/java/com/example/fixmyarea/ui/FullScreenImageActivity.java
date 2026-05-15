package com.example.fixmyarea.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fixmyarea.R;
import com.example.fixmyarea.adapters.PostImageAdapter;

import java.util.ArrayList;
import java.util.List;

public class FullScreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URLS = "extra_image_urls";
    public static final String EXTRA_INITIAL_POSITION = "extra_initial_position";

    private ViewPager2 viewPager;
    private TextView indicatorText;
    private ImageButton closeButton;
    private PostImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        viewPager = findViewById(R.id.fullscreenViewPager);
        indicatorText = findViewById(R.id.fullscreenIndicator);
        closeButton = findViewById(R.id.closeButton);

        List<String> imageUrls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
        int initialPosition = getIntent().getIntExtra(EXTRA_INITIAL_POSITION, 0);

        if (imageUrls == null || imageUrls.isEmpty()) {
            finish();
            return;
        }

        imageAdapter = new PostImageAdapter();
        imageAdapter.setImageUrls(imageUrls);
        viewPager.setAdapter(imageAdapter);
        viewPager.setCurrentItem(initialPosition, false);

        if (imageUrls.size() > 1) {
            indicatorText.setVisibility(View.VISIBLE);
            updateIndicator(initialPosition, imageUrls.size());
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updateIndicator(position, imageUrls.size());
                }
            });
        } else {
            indicatorText.setVisibility(View.GONE);
        }

        closeButton.setOnClickListener(v -> finish());
    }

    private void updateIndicator(int position, int total) {
        indicatorText.setText((position + 1) + " / " + total);
    }
}
