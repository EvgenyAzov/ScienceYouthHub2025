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
                tabTitles = Arrays.asList("Events","Instructors");
                fragmentClasses = Arrays.asList(
                        SupervisorEventsFragment.class,
                        InstructorsFragment.class

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
                tabTitles = Arrays.asList("Feedbacks", "Photos");
                fragmentClasses = Arrays.asList(
                        InstructorFeedbacksFragment.class,
                        InstructorPhotosFragment.class
                );
                break;
            case "Parent":
                tabTitles = Arrays.asList("All activities",     "My children",    "Photos",   "Notifications");
                fragmentClasses = Arrays.asList(
                        ActivitiesFragment.class,
                        ParentChildrenFragment.class,
                        ParentPhotosFragment.class,
                        NotificationsFragment.class
                );
                break;
            case "Student":
            default:
                tabTitles = Arrays.asList("Activities", "My enrollments", "Feedbacks");
                fragmentClasses = Arrays.asList(
                        ActivitiesFragment.class,
                        ActivitiesFragment.class,
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
