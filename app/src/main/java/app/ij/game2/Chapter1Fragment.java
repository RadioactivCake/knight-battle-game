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
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Chapter1Fragment extends Fragment {

    private Button tacticalBattleButton, chapter1CollectionButton, chapter1ChestButton, chapter1ProfileButton;
    private TextView coinDisplay, axolotlLordDisplay;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chapter1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("GameData", Context.MODE_PRIVATE);

        // Check if ALL requirements are met
        if (isChapterFullyUnlocked()) {
            showUnlockedChapterUI(view);
        } else {
            showLockedChapterUI(view);
        }
    }

    private boolean isChapterFullyUnlocked() {
        boolean completedProlog = hasPlayerCompletedProlog();
        boolean hasLord = hasAxolotlLord();

        // Both conditions must be met
        return completedProlog && hasLord;
    }

    private void showLockedChapterUI(View view) {
        // Show locked screen (simple text, no scrolling)
        LinearLayout lockedLayout = view.findViewById(R.id.lockedLayout);
        LinearLayout unlockedLayout = view.findViewById(R.id.unlockedLayout);

        lockedLayout.setVisibility(View.VISIBLE);
        unlockedLayout.setVisibility(View.GONE);

        TextView lockMessage = view.findViewById(R.id.lockMessage);

        String chapter1Knights = sharedPreferences.getString("owned_chapter1_knights", "");
        String progressText = sharedPreferences.getString("furthest_progress_text", "World 1 - Stage 1");

        android.util.Log.d("ChapterDebug", "=== CHAPTER 1 UNLOCK DEBUG ===");
        android.util.Log.d("ChapterDebug", "Chapter 1 knights string: '" + chapter1Knights + "'");
        android.util.Log.d("ChapterDebug", "Progress text: '" + progressText + "'");
        android.util.Log.d("ChapterDebug", "Contains Axolotl Lord: " + chapter1Knights.contains("Axolotl Lord"));
        android.util.Log.d("ChapterDebug", "Prolog completed: " + hasPlayerCompletedProlog());

        // Only check the two essential requirements
        boolean completedProlog = hasPlayerCompletedProlog();
        boolean hasLord = hasAxolotlLord();

        StringBuilder message = new StringBuilder();
        message.append("🔒 CHAPTER 1 LOCKED 🔒\n\n");
        message.append("Complete these requirements:\n\n");

        // Requirement 1: Complete Prolog
        if (completedProlog) {
            message.append("✅ Complete the Prolog (beat World 4)\n");
        } else {
            message.append("❌ Complete the Prolog (beat World 4)\n");
        }

        // Requirement 2: Get Axolotl Lord
        if (hasLord) {
            message.append("✅ Obtain Axolotl Lord");
        } else {
            message.append("❌ Obtain Axolotl Lord");
        }

        lockMessage.setText(message.toString());

        // More debug
        android.util.Log.d("ChapterDebug", "Final hasLord result: " + hasLord);
        android.util.Log.d("ChapterDebug", "Final completedProlog result: " + completedProlog);
    }


    private boolean hasPlayerCompletedProlog() {
        String progressText = sharedPreferences.getString("furthest_progress_text", "World 1 - Stage 1");

        // Add debug logging
        android.util.Log.d("PrologDebug", "Progress text: '" + progressText + "'");

        if (progressText.startsWith("World ")) {
            try {
                String worldPart = progressText.substring(6);
                String[] parts = worldPart.split(" - Stage ");

                int worldNumber = Integer.parseInt(parts[0]);
                int stageNumber = Integer.parseInt(parts[1]);

                android.util.Log.d("PrologDebug", "Extracted: World " + worldNumber + ", Stage " + stageNumber);

                // Check if completed World 4 Stage 5 (the mini boss)
                boolean completed = (worldNumber > 4) || (worldNumber == 4 && stageNumber >= 5);

                android.util.Log.d("PrologDebug", "Prolog completed? " + completed);

                return completed;
            } catch (Exception e) {
                android.util.Log.d("PrologDebug", "Error parsing: " + e.getMessage());
                return false;
            }
        }

        android.util.Log.d("PrologDebug", "Progress text doesn't start with 'World'");
        return false;
    }

    private boolean hasAxolotlLord() {
        String chapter1Knights = sharedPreferences.getString("owned_chapter1_knights", "");
        return chapter1Knights.contains("Axolotl Lord");
    }

    private void showUnlockedChapterUI(View view) {
        // Show Chapter 1 lobby (like prolog but for tactical battles)
        LinearLayout lockedLayout = view.findViewById(R.id.lockedLayout);
        LinearLayout unlockedLayout = view.findViewById(R.id.unlockedLayout);

        lockedLayout.setVisibility(View.GONE);
        unlockedLayout.setVisibility(View.VISIBLE);

        // Initialize UI elements
        tacticalBattleButton = view.findViewById(R.id.tacticalBattleButton);
        chapter1CollectionButton = view.findViewById(R.id.chapter1CollectionButton);
        chapter1ChestButton = view.findViewById(R.id.chapter1ChestButton);
        chapter1ProfileButton = view.findViewById(R.id.chapter1ProfileButton);
        coinDisplay = view.findViewById(R.id.chapter1CoinDisplay);
        // REMOVED: axolotlLordDisplay = view.findViewById(R.id.axolotlLordDisplay);

        // Set up click listeners (same as before)
        tacticalBattleButton.setOnClickListener(v -> {
            android.widget.Toast.makeText(getActivity(),
                    "🏰 Tactical Battle System Coming Soon! 🏰",
                    android.widget.Toast.LENGTH_SHORT).show();
        });

        chapter1CollectionButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CollectionActivity.class);
            intent.putExtra("chapter_filter", "CHAPTER1");
            startActivity(intent);
        });

        chapter1ChestButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChestActivity.class);
            startActivity(intent);
        });

        chapter1ProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        // Update displays
        updateCoinDisplay();
        // REMOVED: updateAxolotlLordDisplay();
    }


    @Override
    public void onResume() {
        super.onResume();

        // Re-check unlock status when returning to fragment
        if (getView() != null) {
            if (isChapterFullyUnlocked()) {
                showUnlockedChapterUI(getView());
            } else {
                showLockedChapterUI(getView());
            }
        }
    }


    private void updateCoinDisplay() {
        if (coinDisplay != null) {
            int coins = sharedPreferences.getInt("coins", 0);
            coinDisplay.setText(String.valueOf(coins));
        }
    }

    private boolean isUnlocked() {
        return isChapterFullyUnlocked();
    }
}