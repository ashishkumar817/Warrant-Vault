package com.warrantvault.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.warrantvault.R;
import com.warrantvault.adapters.OnboardingAdapter;
import com.warrantvault.databinding.ActivityOnboardingBinding;
import com.warrantvault.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private SessionManager sessionManager;
    private final ImageView[] dots = new ImageView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        dots[0] = binding.indicatorDot0;
        dots[1] = binding.indicatorDot1;
        dots[2] = binding.indicatorDot2;

        List<OnboardingAdapter.SlideItem> slides = new ArrayList<>();
        slides.add(new OnboardingAdapter.SlideItem(
                R.drawable.illustration_bills,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1)
        ));
        slides.add(new OnboardingAdapter.SlideItem(
                R.drawable.illustration_warranty,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2)
        ));
        slides.add(new OnboardingAdapter.SlideItem(
                R.drawable.illustration_documents,
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3)
        ));

        OnboardingAdapter adapter = new OnboardingAdapter(slides);
        binding.viewPager.setAdapter(adapter);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
                if (position == 2) {
                    binding.btnNext.setText(R.string.onboarding_get_started);
                    binding.btnSkip.setVisibility(View.INVISIBLE);
                } else {
                    binding.btnNext.setText(R.string.onboarding_next);
                    binding.btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current < 2) {
                binding.viewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        binding.btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < dots.length; i++) {
            if (i == position) {
                dots[i].setImageResource(R.drawable.indicator_dot_selected);
            } else {
                dots[i].setImageResource(R.drawable.indicator_dot_unselected);
            }
        }
    }

    private void finishOnboarding() {
        sessionManager.setOnboardingCompleted(true);
        Intent intent = new Intent(OnboardingActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}
