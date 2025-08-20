package app.ij.game2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameInput;
    private Spinner titleSpinner;
    private Button backButton, saveButton;
    private TextView fullNameDisplay;
    private SharedPreferences sharedPreferences;

    private String[] availableTitles = {"Great", "Mighty", "Slayer"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide navigation bar and make fullscreen
        hideSystemUI();

        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("GameData", MODE_PRIVATE);

        initializeUI();
        loadCurrentProfile();
        updateFullNameDisplay();
    }

    private void hideSystemUI() {
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

    private void initializeUI() {
        nameInput = findViewById(R.id.nameInput);
        titleSpinner = findViewById(R.id.titleSpinner);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        fullNameDisplay = findViewById(R.id.fullNameDisplay);

        // Setup title spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, availableTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        titleSpinner.setAdapter(adapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        // Update display when name changes
        nameInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFullNameDisplay();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Update display when title changes
        titleSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateFullNameDisplay();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadCurrentProfile() {
        // Load saved name and title
        String savedName = sharedPreferences.getString("player_name", "Warrior");
        String savedTitle = sharedPreferences.getString("player_title", "Great");

        nameInput.setText(savedName);

        // Set spinner to saved title
        for (int i = 0; i < availableTitles.length; i++) {
            if (availableTitles[i].equals(savedTitle)) {
                titleSpinner.setSelection(i);
                break;
            }
        }
    }

    private void updateFullNameDisplay() {
        String name = nameInput.getText().toString().trim();
        String title = (String) titleSpinner.getSelectedItem();

        if (name.isEmpty()) {
            name = "Warrior";
        }

        String fullName = name + " the " + title;
        fullNameDisplay.setText(fullName);
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String title = (String) titleSpinner.getSelectedItem();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate name (single word only)
        if (name.contains(" ")) {
            Toast.makeText(this, "Name must be a single word!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for admin cheat code
        if (name.equals("admin") && title.equals("Mighty")) {
            activateAdminCheat();
            return;
        }

        // NEW: Check for money cheat code
        if (name.equals("money") && title.equals("Great")) {
            activateMoneyCheat();
            return;
        }

        // Regular save logic (existing code)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("player_name", name);
        editor.putString("player_title", title);

        boolean saved = editor.commit();

        if (saved) {
            android.util.Log.d("ProfileSystem", "Profile saved: " + name + " the " + title);
            finish();
        } else {
            Toast.makeText(this, "Failed to save profile!", Toast.LENGTH_SHORT).show();
        }
    }

    private void activateAdminCheat() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Change name from admin to player
        editor.putString("player_name", "Player");
        editor.putString("player_title", "Mighty");

        // Create temporary admin knight flag
        editor.putBoolean("has_admin_knight", true);

        // Add King's Guard to owned knights (temporarily)
        String existingKnights = sharedPreferences.getString("owned_knights", "Brave Knight");
        if (!existingKnights.contains("King's Guard")) {
            String updatedKnights = existingKnights + ",King's Guard";
            editor.putString("owned_knights", updatedKnights);
        }

        // Set King's Guard as equipped
        editor.putString("equipped_knight", "King's Guard");

        // Don't save King's Guard stats to prevent permanent saving
        // The game will handle this knight specially

        boolean saved = editor.commit();

        if (saved) {
            Toast.makeText(this, "🔑 Admin cheat activated! King's Guard equipped!", Toast.LENGTH_LONG).show();
            android.util.Log.d("AdminCheat", "King's Guard activated for testing");
            finish();
        } else {
            Toast.makeText(this, "Failed to activate cheat!", Toast.LENGTH_SHORT).show();
        }
    }

    private void activateMoneyCheat() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Change name from money to player (similar to admin cheat)
        editor.putString("player_name", "Player");
        editor.putString("player_title", "Great");

        // Add 1000 coins to current total
        int currentCoins = sharedPreferences.getInt("coins", 0);
        int newCoinTotal = currentCoins + 1000;
        editor.putInt("coins", newCoinTotal);

        // Optional: Add a flag to track money cheat usage (for debugging)
        int timesUsed = sharedPreferences.getInt("money_cheat_used", 0);
        editor.putInt("money_cheat_used", timesUsed + 1);

        boolean saved = editor.commit();

        if (saved) {
            Toast.makeText(this, "💰 Money cheat activated! +1000 coins!", Toast.LENGTH_LONG).show();
            android.util.Log.d("MoneyCheat", "Added 1000 coins. New total: " + newCoinTotal +
                    " (Used " + (timesUsed + 1) + " times)");
            finish();
        } else {
            Toast.makeText(this, "Failed to activate money cheat!", Toast.LENGTH_SHORT).show();
        }
    }
}