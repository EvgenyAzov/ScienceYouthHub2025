package com.example.scienceyouthhub;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final List<Integer> tabIds;
    private final String role;

    // tabIds например: [0, 1, 2, 3] или [0, 1, 4, 2, 3]
    // где 0 = Home, 1 = Activities, 2 = Photos, 3 = Feedback, 4 = Users (для админа)
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager,
                            @NonNull Lifecycle lifecycle,
                            List<Integer> tabIds,
                            String role) {
        super(fragmentManager, lifecycle);
        this.tabIds = tabIds;
        this.role = role;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int tabId = tabIds.get(position);
        switch (tabId) {
            case 0:
                return new HomeFragment();
            case 1:
                return new ActivitiesFragment();
            case 2:
                return new PhotosFragment();
            case 3:
                return new FeedbackFragment();
            case 4:
                // Только для Admin/Руководитель (создай свой UsersFragment)
                return new UsersFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return tabIds.size();
    }
}
