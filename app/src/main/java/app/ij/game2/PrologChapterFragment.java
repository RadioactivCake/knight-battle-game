package app.ij.game2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class PrologChapterFragment extends Fragment {

    // All your existing MainActivity variables
    private Button playButton, collectionButton, chestButton, profileButton;
    private TextView coinDisplay, progressDisplay;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prolog_chapter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("GameData", Context.MODE_PRIVATE);

        // Find all your existing UI elements
        playButton = view.findViewById(R.id.playButton);
        collectionButton = view.findViewById(R.id.collectionButton);
        chestButton = view.findViewById(R.id.chestButton);
        profileButton = view.findViewById(R.id.profileButton);
        coinDisplay = view.findViewById(R.id.coinDisplay);
        progressDisplay = view.findViewById(R.id.progressDisplay);

        // Set up all your existing click listeners
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TESTING: Always show opening story
                Intent storyIntent = new Intent(getActivity(), StoryActivity.class);
                storyIntent.putExtra("story_type", "OPENING");
                startActivity(storyIntent);
            }
        });

        collectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CollectionActivity.class);
                startActivity(intent);
            }
        });

        chestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChestActivity.class);
                startActivity(intent);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Update displays
        updateCoinDisplay();
        updateProgressDisplay();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update displays when returning from other activities
        updateCoinDisplay();
        updateProgressDisplay();
    }

    // Copy your existing methods from MainActivity
    private void updateCoinDisplay() {
        int coins = sharedPreferences.getInt("coins", 0);
        coinDisplay.setText(String.valueOf(coins));
        android.util.Log.d("CoinSystem", "Displaying coins: " + coins);
    }

    private void updateProgressDisplay() {
        String bestProgressText = sharedPreferences.getString("furthest_progress_text", "World 1 - Stage 1");
        progressDisplay.setText("🏆 Best: " + bestProgressText);
    }
}