package com.example.scienceyouthhub;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final List<String> tabTitles;
    private final String role;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> tabTitles, String role) {
        super(fragmentActivity);
        this.tabTitles = tabTitles;
        this.role = role;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Сопоставляем вкладки с фрагментами по роли и индексу
        if ("Admin".equals(role)) {
            switch (position) {
                case 0: return new ActivitiesFragment();    // Кружки
                case 1: return new UsersFragment();         // Пользователи
                case 2: return new FeedbackFragment();      // Отзывы
                case 3: return new PhotosFragment();        // Фотографии
                default: return new ActivitiesFragment();
            }
        } else if ("Instructor".equals(role)) {
            switch (position) {
                case 0: return new ActivitiesFragment();    // Кружки
                case 1: return new UsersFragment();         // Мои студенты (можно заменить на MyStudentsFragment)
                case 2: return new FeedbackFragment();      // Отзывы
                default: return new ActivitiesFragment();
            }
        } else if ("Student".equals(role)) {
            switch (position) {
                case 0: return new ActivitiesFragment();    // Кружки
                case 1: return new ActivitiesFragment();    // Мои записи (можно заменить на MyEnrollmentsFragment)
                case 2: return new FeedbackFragment();      // Отзывы
                default: return new ActivitiesFragment();
            }
        } else if ("Parent".equals(role)) {
            switch (position) {
                case 0: return new ActivitiesFragment();    // Кружки
                case 1: return new ChildrenFragment();      // Мои дети
                // case 2: return new FeedbackFragment();  // Если добавишь вкладку "Отзывы" для родителя
                default: return new ActivitiesFragment();
            }
        } else {
            return new ActivitiesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.size();
    }
}
