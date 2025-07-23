package com.example.scienceyouthhub;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Arrays;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final List<String> tabTitles;
    private final List<Class<? extends Fragment>> fragmentClasses;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String role) {
        super(fragmentActivity);

        switch (role) {
            case "Supervisor":
                tabTitles = Arrays.asList("Events");
                fragmentClasses = Arrays.asList(
                        ActivitiesFragment.class
                );
                break;
            case "Admin":
                tabTitles = Arrays.asList("Activities", "Users", "Feedbacks", "Photos");
                fragmentClasses = Arrays.asList(
                        ActivitiesFragment.class,
                        UsersFragment.class,
                        FeedbackFragment.class,
                        PhotosFragment.class
                );
                break;
            case "Instructor":
                tabTitles = Arrays.asList("Activities", "My students", "Feedbacks");
                fragmentClasses = Arrays.asList(
                        ActivitiesFragment.class,
                        UsersFragment.class, // Or your MyStudentsFragment.class
                        FeedbackFragment.class
                );
                break;
            case "Parent":
                tabTitles = Arrays.asList("All activities", "Search activities", "My children", "Photos");
                fragmentClasses = Arrays.asList(
                        ActivitiesFragment.class, // For all activities
                        ActivitiesFragment.class, // For search — you can replace with SearchActivitiesFragment.class
                        ChildrenFragment.class,   // Own children
                        PhotosFragment.class
                );
                break;
            case "Student":
            default:
                tabTitles = Arrays.asList("Activities", "My enrollments", "Feedbacks");
                fragmentClasses = Arrays.asList(
                        ActivitiesFragment.class,
                        ActivitiesFragment.class, // For "My enrollments" you can replace with MyEnrollmentsFragment.class
                        FeedbackFragment.class
                );
                break;
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        try {
            return fragmentClasses.get(position).newInstance();
        } catch (Exception e) {
            // In case the class is not found
            return new ActivitiesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.size();
    }

    public String getTabTitle(int position) {
        return tabTitles.get(position);
    }
}
