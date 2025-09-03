package app.ij.game2;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ChapterPagerAdapter extends FragmentStateAdapter {
    private SharedPreferences sharedPreferences;

    public ChapterPagerAdapter(@NonNull FragmentActivity fragmentActivity, SharedPreferences sharedPreferences) {
        super(fragmentActivity);
        this.sharedPreferences = sharedPreferences;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PrologChapterFragment(); // Your existing content
            case 1:
                return new Chapter1Fragment(); // New chapter content
            default:
                return new PrologChapterFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Prolog + Chapter 1
    }
}