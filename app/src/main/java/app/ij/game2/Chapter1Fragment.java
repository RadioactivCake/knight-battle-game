package app.ij.game2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Chapter1Fragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chapter1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("GameData", Context.MODE_PRIVATE);

        // Find the ScrollView to handle conflicts with ViewPager
        ScrollView scrollView = (ScrollView) view;

        // Prevent ViewPager from intercepting vertical scroll events
        scrollView.setOnTouchListener((v, event) -> {
            // Disable parent ViewPager when scrolling vertically
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        // Check if chapter is unlocked
        String chapter1Knights = sharedPreferences.getString("owned_chapter1_knights", "");
        boolean hasAxolotlLord = chapter1Knights.contains("Axolotl Lord");

        TextView statusText = view.findViewById(R.id.chapterStatusText);

        if (!hasAxolotlLord) {
            statusText.setText("🔒 CHAPTER 1 LOCKED 🔒\n\n" +
                    "Complete the ultimate evolution:\n\n" +
                    "• Evolve Axolotl Knight to max (11 copies)\n" +
                    "• Obtain Character Development trait (Divine rarity)\n" +
                    "• Transform into Axolotl Lord\n\n" +
                    "The path to tactical mastery awaits!\n\n" +
                    "How to get Character Development trait:\n" +
                    "1. Evolve your Axolotl Knight (need 11 copies)\n" +
                    "2. Roll traits for Evolved Axolotl Knight (100 coins each)\n" +
                    "3. Character Development has 4% chance (Divine tier)\n" +
                    "4. Once you have both, ultimate evolution unlocks\n\n" +
                    "This will unlock advanced tactical battles with positioning, strategy, and army management!");
        } else {
            statusText.setText("🏆 CHAPTER 1 UNLOCKED! 🏆\n\n" +
                    "Welcome to tactical warfare!\n" +
                    "Your Axolotl Lord awaits commands!\n\n" +
                    "(HoMM-style battles coming soon...)\n\n" +
                    "Features planned:\n" +
                    "• Grid-based battlefield positioning\n" +
                    "• Army composition strategy\n" +
                    "• Turn-based tactical combat\n" +
                    "• Spell system and special abilities\n" +
                    "• Multiple victory conditions\n\n" +
                    "This is where the real strategic depth begins!");
        }
    }
}