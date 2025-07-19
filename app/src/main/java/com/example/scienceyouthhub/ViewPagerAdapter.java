package com.example.scienceyouthhub;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new ActivitiesFragment();
            case 2: return new PhotosFragment();
            case 3: return new FeedbackFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // 4 tabs: Home, Activities, Photos, Feedback
    }
}