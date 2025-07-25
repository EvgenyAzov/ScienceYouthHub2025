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
                tabTitles = Arrays.asList("All activities","Events","Instructors");
                fragmentClasses = Arrays.asList(
                        AllActivitiesFragment.class,
                        SupervisorEventsFragment.class,
                        InstructorsFragment.class

                );
                break;
            case "Admin":
                tabTitles = Arrays.asList("All activities","Approve act", "Users", "Act status");
                fragmentClasses = Arrays.asList(
                        AllActivitiesFragment.class ,
                        AdminPendingActivitiesFragment.class,
                        UsersFragment.class,
                        AdminActivitiesWithStatusFragment.class

                );
                break;
            case "Instructor":
                tabTitles = Arrays.asList("All activities","Feedbacks", "Photos");
                fragmentClasses = Arrays.asList(
                        AllActivitiesFragment.class ,
                        InstructorFeedbacksFragment.class,
                        InstructorPhotosFragment.class
                );
                break;
            case "Parent":
                tabTitles = Arrays.asList("All activities",     "My children",    "Photos",   "Feedbacks");
                fragmentClasses = Arrays.asList(
                        AllActivitiesFragment.class,
                        ParentChildrenFragment.class,
                        ParentPhotosFragment.class,
                        ParentFeedbacksFragment.class
                );
                break;
            case "Student":
            default:
                tabTitles = Arrays.asList("All activities", "My enrollments", "Feedbacks","INFO");
                fragmentClasses = Arrays.asList(
                        AllActivitiesFragment.class,
                        StudentScheduleFragment.class,
                        FeedbackFragment.class,
                        StudentProfileFragment.class
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
