package app.ij.game2;
import java.util.Map;
import java.util.HashMap;        // ADD THIS
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;  // ADD THIS LINE
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.widget.LinearLayout;
public class MainActivity extends AppCompatActivity {

    private Button playButton, collectionButton, chestButton, profileButton;
    private TextView coinDisplay;
    private SharedPreferences sharedPreferences;
    private static boolean isFirstTime = true; // Track if this is first app launch
    private TextView progressDisplay;
    private ViewPager2 chapterViewPager;
    private ChapterPagerAdapter pagerAdapter;
    private TabLayout chapterTabs;
    private LinearLayout dotIndicatorContainer;
    private View[] dotIndicators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide navigation bar and make fullscreen
        hideSystemUI();

        // CHANGED: Use new layout with ViewPager
        setContentView(R.layout.activity_main_with_chapters);

        // Initialize SharedPreferences for saving coins
        sharedPreferences = getSharedPreferences("GameData", MODE_PRIVATE);

        // Only clean up admin knight on true app restart, not when returning from activities
        if (isFirstTime) {
            cleanupAdminKnight();
            isFirstTime = false;
        }

        updateKnightStats();
        fixDuplicateKnights();

        // NEW: Initialize chapter system
        initializeChapterSystem();

        showAppIntroMessage();
    }

    private void hideSystemUI() {
        // Enable fullscreen mode
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update chapter access when returning from other activities
        if (chapterViewPager != null) {
            updateChapterAccess();

            // Refresh the current fragment
            if (pagerAdapter != null) {
                pagerAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateCoinDisplay() {
        int coins = sharedPreferences.getInt("coins", 0);
        coinDisplay.setText(String.valueOf(coins));

        // Debug logging
        android.util.Log.d("CoinSystem", "Displaying coins: " + coins);
    }

    private void cleanupAdminKnight() {
        boolean hasAdminKnight = sharedPreferences.getBoolean("has_admin_knight", false);
        if (hasAdminKnight) {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Remove admin knight flag
            editor.putBoolean("has_admin_knight", false);

            // Remove King's Guard from owned knights
            String ownedKnights = sharedPreferences.getString("owned_knights", "Brave Knight");
            if (ownedKnights.contains("King's Guard")) {
                String cleanedKnights = ownedKnights.replace(",King's Guard", "").replace("King's Guard,", "").replace("King's Guard", "");
                if (cleanedKnights.isEmpty()) {
                    cleanedKnights = "Brave Knight";
                }
                editor.putString("owned_knights", cleanedKnights);
            }

            // If King's Guard was equipped, switch back to Brave Knight
            String equippedKnight = sharedPreferences.getString("equipped_knight", "Brave Knight");
            if (equippedKnight.equals("King's Guard")) {
                editor.putString("equipped_knight", "Brave Knight");
            }

            editor.apply();
            android.util.Log.d("AdminCheat", "King's Guard cleaned up on app restart");
        }
    }

    // Replace your updateKnightStats() method in MainActivity.java with this simple version:

    private void updateKnightStats() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        android.util.Log.d("KnightMigration", "=== Starting General Knight Migration ===");

        // STEP 1: Fix any knight name mismatches with database
        fixKnightNameMismatches(editor);

        // STEP 2: Update ALL knights to match database stats (always run)
        updateAllKnightsToDatabase(editor);

        editor.apply();
        android.util.Log.d("KnightMigration", "=== General Migration Complete ===");
    }

    // This method automatically fixes knights that don't match the database
    private void fixKnightNameMismatches(SharedPreferences.Editor editor) {
        String ownedKnights = sharedPreferences.getString("owned_knights", "Brave Knight");
        String[] knightArray = ownedKnights.split(",");

        StringBuilder updatedOwnedKnights = new StringBuilder();
        String equippedKnight = sharedPreferences.getString("equipped_knight", "Brave Knight");
        String equippedSquire = sharedPreferences.getString("equipped_squire", "");

        boolean hasChanges = false;

        for (String knightName : knightArray) {
            knightName = knightName.trim();

            if (knightName.isEmpty()) continue;

            // Check if this knight exists in database by name
            KnightDatabase.KnightData data = KnightDatabase.getKnightDataByName(knightName);

            if (data != null) {
                // Knight exists in database - keep as is
                if (updatedOwnedKnights.length() > 0) updatedOwnedKnights.append(",");
                updatedOwnedKnights.append(knightName);
            } else if (knightName.startsWith("Evolved ")) {
                // Handle evolved knights - check if base knight exists in database
                String baseName = knightName.replace("Evolved ", "");
                KnightDatabase.KnightData baseData = KnightDatabase.getKnightDataByName(baseName);

                if (baseData != null) {
                    // Base exists in database - keep evolved version as is
                    if (updatedOwnedKnights.length() > 0) updatedOwnedKnights.append(",");
                    updatedOwnedKnights.append(knightName);
                } else {
                    // Base doesn't exist - try to find correct name and convert
                    String correctName = findCorrectKnightName(baseName);
                    if (correctName != null) {
                        String correctEvolvedName = "Evolved " + correctName;

                        android.util.Log.d("KnightMigration", "Converting " + knightName + " → " + correctEvolvedName);

                        // Transfer data and merge if target already exists
                        mergeKnightData(editor, knightName, correctEvolvedName);

                        if (updatedOwnedKnights.length() > 0) updatedOwnedKnights.append(",");
                        updatedOwnedKnights.append(correctEvolvedName);
                        hasChanges = true;

                        // Update equipment
                        if (equippedKnight.equals(knightName)) {
                            editor.putString("equipped_knight", correctEvolvedName);
                        }
                        if (equippedSquire.equals(knightName)) {
                            editor.putString("equipped_squire", correctEvolvedName);
                        }
                    }
                }
            } else {
                // Regular knight not in database - try to find correct name
                String correctName = findCorrectKnightName(knightName);
                if (correctName != null) {
                    android.util.Log.d("KnightMigration", "Converting " + knightName + " → " + correctName);

                    // Transfer data and merge if target already exists
                    mergeKnightData(editor, knightName, correctName);

                    if (updatedOwnedKnights.length() > 0) updatedOwnedKnights.append(",");
                    updatedOwnedKnights.append(correctName);
                    hasChanges = true;

                    // Update equipment
                    if (equippedKnight.equals(knightName)) {
                        editor.putString("equipped_knight", correctName);
                    }
                    if (equippedSquire.equals(knightName)) {
                        editor.putString("equipped_squire", correctName);
                    }
                } else {
                    // Keep unknown knights as legacy
                    android.util.Log.d("KnightMigration", "Keeping legacy knight: " + knightName);
                    if (updatedOwnedKnights.length() > 0) updatedOwnedKnights.append(",");
                    updatedOwnedKnights.append(knightName);
                }
            }
        }

        if (hasChanges) {
            editor.putString("owned_knights", updatedOwnedKnights.toString());
            android.util.Log.d("KnightMigration", "Updated owned knights: " + updatedOwnedKnights.toString());
        }
    }

    // Find the correct knight name in database (handles name changes like Brave Knight → Axolotl Knight)
    private String findCorrectKnightName(String oldName) {
        // Simple mapping for known name changes
        switch (oldName) {
            case "Brave Knight":
                return "Axolotl Knight";  // This is your specific case
            // Add more mappings here as needed:
            // case "Old Name": return "New Name";
            default:
                return null; // No mapping found
        }
    }

    // Merge knight data from old name to new name (handles duplicate knights)
    private void mergeKnightData(SharedPreferences.Editor editor, String fromName, String toName) {
        // Get data from old knight
        int fromQuantity = sharedPreferences.getInt(fromName + "_quantity", 1);
        int fromHp = sharedPreferences.getInt(fromName + "_hp", 100);
        int fromAttack = sharedPreferences.getInt(fromName + "_attack", 20);

        // Get existing data from target knight (if any)
        int toQuantity = sharedPreferences.getInt(toName + "_quantity", 0);
        int toHp = sharedPreferences.getInt(toName + "_hp", fromHp);
        int toAttack = sharedPreferences.getInt(toName + "_attack", fromAttack);

        if (toQuantity > 0) {
            // Target knight already exists - merge quantities (cap at 11)
            int mergedQuantity = Math.min(fromQuantity + toQuantity, 11);
            editor.putInt(toName + "_quantity", mergedQuantity);

            android.util.Log.d("KnightMigration", "Merged " + fromName + " (" + fromQuantity + ") + " +
                    toName + " (" + toQuantity + ") = " + mergedQuantity + " total");
        } else {
            // Target knight doesn't exist - transfer all data
            editor.putInt(toName + "_quantity", fromQuantity);
            editor.putInt(toName + "_hp", fromHp);
            editor.putInt(toName + "_attack", fromAttack);
            editor.putString(toName + "_image", "player_character");

            android.util.Log.d("KnightMigration", "Transferred " + fromName + " → " + toName +
                    " (Quantity: " + fromQuantity + ")");
        }

        // Remove old knight data
        editor.remove(fromName + "_quantity");
        editor.remove(fromName + "_hp");
        editor.remove(fromName + "_attack");
        editor.remove(fromName + "_image");
    }

    // Update all knights to database stats (always runs - no version check)
    private void updateAllKnightsToDatabase(SharedPreferences.Editor editor) {
        Map<String, KnightDatabase.KnightData> allKnights = KnightDatabase.getAllKnights();
        String ownedKnights = sharedPreferences.getString("owned_knights", "Axolotl Knight");

        android.util.Log.d("KnightMigration", "Updating all knight stats from database...");

        // Update all base knights from database
        for (Map.Entry<String, KnightDatabase.KnightData> entry : allKnights.entrySet()) {
            KnightDatabase.KnightData data = entry.getValue();

            // Update base knight if player owns it
            if (ownedKnights.contains(data.name)) {
                updateKnightStatsIfDifferent(editor, data.name, data.baseHP, data.baseAttack);
            }

            // Update evolved version if player owns it
            String evolvedName = "Evolved " + data.name;
            if (ownedKnights.contains(evolvedName)) {
                // Evolved knights get 2x base stats
                updateKnightStatsIfDifferent(editor, evolvedName, data.baseHP * 2, data.baseAttack * 2);
            }
        }
    }

    // Helper method to only update stats if they're different
    private void updateKnightStatsIfDifferent(SharedPreferences.Editor editor, String knightName, int newHp, int newAttack) {
        int currentHp = sharedPreferences.getInt(knightName + "_hp", 0);
        int currentAttack = sharedPreferences.getInt(knightName + "_attack", 0);

        if (currentHp != newHp || currentAttack != newAttack) {
            editor.putInt(knightName + "_hp", newHp);
            editor.putInt(knightName + "_attack", newAttack);

            android.util.Log.d("KnightMigration", "Updated " + knightName + ": " +
                    currentHp + "→" + newHp + " HP, " + currentAttack + "→" + newAttack + " ATK");
        }
    }

    // Remove these methods - we don't need them anymore:
    // - handleLegacyKnights()
    // - updateEvolvedKnightStats()
    // - forceKnightStatsMigration()
    // Add this method to MainActivity.java (temporary fix):

    private void fixDuplicateKnights() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String ownedKnights = sharedPreferences.getString("owned_knights", "");

        android.util.Log.d("DuplicateFix", "Before fix: " + ownedKnights);

        // Split and process knights
        String[] knightArray = ownedKnights.split(",");
        Map<String, Integer> knightCounts = new HashMap<>();
        Map<String, Integer> knightQuantities = new HashMap<>();

        // Count occurrences and sum quantities
        for (String knight : knightArray) {
            knight = knight.trim();
            if (knight.isEmpty()) continue;

            knightCounts.put(knight, knightCounts.getOrDefault(knight, 0) + 1);

            int currentQuantity = sharedPreferences.getInt(knight + "_quantity", 1);
            knightQuantities.put(knight, knightQuantities.getOrDefault(knight, 0) + currentQuantity);
        }

        // Rebuild the owned knights list without duplicates
        StringBuilder cleanedKnights = new StringBuilder();

        for (Map.Entry<String, Integer> entry : knightCounts.entrySet()) {
            String knightName = entry.getKey();
            int duplicateCount = entry.getValue();
            int totalQuantity = knightQuantities.get(knightName);

            if (duplicateCount > 1) {
                android.util.Log.d("DuplicateFix", "Found " + duplicateCount + " duplicate entries of " + knightName);
                android.util.Log.d("DuplicateFix", "Total quantity: " + totalQuantity);

                // Cap total quantity at 11
                totalQuantity = Math.min(totalQuantity, 11);

                // Update the quantity
                editor.putInt(knightName + "_quantity", totalQuantity);

                android.util.Log.d("DuplicateFix", "Merged " + knightName + " to quantity: " + totalQuantity);
            }

            // Add to cleaned list (only once)
            if (cleanedKnights.length() > 0) cleanedKnights.append(",");
            cleanedKnights.append(knightName);
        }

        // Update the owned knights list
        editor.putString("owned_knights", cleanedKnights.toString());
        editor.apply();

        android.util.Log.d("DuplicateFix", "After fix: " + cleanedKnights.toString());
    }

    private void showAppIntroMessage() {
        // You can customize this message later
        String introMessage = "Your job is to go around and defeat enemies! ⚔\uFE0F\u200B\n\n" +
                "You defeat enemies by choosing one of three attacks in battle! \uD83D\uDCA5\n\n" +
                "Don't worry, for every enemy you defeat you will receive coins! \uD83E\uDE99\u200B\n\n" +
                "Make sure to retreat when you need to collect your payment! \uD83C\uDFF0\u200B\n\n" +
                "A dead knight doesn't get payed! \uD83D\uDC80\u200B\n\n" +
                "With these coins you will be able to open chests and get new knights! \uD83C\uDF1F\u200B\n\n" +
                "Don't work alone! Equip a squire to receive a special buff! \uD83D\uDCAA\u200B\n\n" +
                "That's it, Go! Protect the kingdom! \uD83D\uDEE1\uFE0F\u200B\n\n" +
                "If you find any bugs or have any ideas for improvement i would love to hear about them \uD83E\uDD16\u200B";

        new AlertDialog.Builder(this)
                .setTitle("Welcome! 🎮")
                .setMessage(introMessage)
                .setPositiveButton("Let's Play!", null)
                .setCancelable(true)  // Allow dismissing with back button
                .show();
    }

    private void updateProgressDisplay() {
        String bestProgressText = sharedPreferences.getString("furthest_progress_text", "World 1 - Stage 1");
        progressDisplay.setText("🏆 Best: " + bestProgressText);
    }

    private void initializeChapterSystem() {
        // Find ViewPager and dot container
        chapterViewPager = findViewById(R.id.chapterViewPager);
        dotIndicatorContainer = findViewById(R.id.dotIndicatorContainer);

        // Create adapter
        pagerAdapter = new ChapterPagerAdapter(this, sharedPreferences);
        chapterViewPager.setAdapter(pagerAdapter);

        // Create dot indicators
        createDotIndicators();

        // Set up page change listener for dots
        chapterViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDotIndicators(position);
            }
        });

        // Update chapter access based on player progress
        updateChapterAccess();
    }

    private void updateChapterAccess() {
        // Check if ALL requirements are met for Chapter 1
        String chapter1Knights = sharedPreferences.getString("owned_chapter1_knights", "");
        boolean hasAxolotlLord = chapter1Knights.contains("Axolotl Lord");
        boolean hasCompletedProlog = hasPlayerCompletedProlog();

        boolean isChapter1Unlocked = hasAxolotlLord && hasCompletedProlog;

        // Make Chapter 1 dot semi-transparent if locked
        if (dotIndicators != null && dotIndicators.length > 1) {
            if (!isChapter1Unlocked) {
                dotIndicators[1].setAlpha(0.3f); // Dim the Chapter 1 dot
            } else {
                dotIndicators[1].setAlpha(1.0f); // Full brightness when unlocked
            }
        }
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

    private void createDotIndicators() {
        int pageCount = pagerAdapter.getItemCount();
        dotIndicators = new View[pageCount];

        for (int i = 0; i < pageCount; i++) {
            View dot = new View(this);

            // Create circular dot
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);

            // Make it circular
            dot.setBackground(createDotDrawable(false));

            dotIndicators[i] = dot;
            dotIndicatorContainer.addView(dot);
        }

        // Set first dot as active
        updateDotIndicators(0);
    }

    private android.graphics.drawable.Drawable createDotDrawable(boolean isActive) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);

        if (isActive) {
            drawable.setColor(0xFFFFD700); // Gold for active
        } else {
            drawable.setColor(0x66FFFFFF); // Semi-transparent white for inactive
        }

        return drawable;
    }

    private void updateDotIndicators(int activePosition) {
        for (int i = 0; i < dotIndicators.length; i++) {
            dotIndicators[i].setBackground(createDotDrawable(i == activePosition));
        }
    }


}