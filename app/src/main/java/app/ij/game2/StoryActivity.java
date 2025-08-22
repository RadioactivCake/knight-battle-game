package app.ij.game2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StoryActivity extends AppCompatActivity {

    private TextView storyTitleText, storyContentText;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide navigation bar and make fullscreen
        hideSystemUI();

        setContentView(R.layout.activity_story);

        initializeUI();
        loadStoryData();
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
        storyTitleText = findViewById(R.id.storyTitleText);
        storyContentText = findViewById(R.id.storyContentText);
        continueButton = findViewById(R.id.continueButton);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleContinue();
            }
        });
    }

    private void loadStoryData() {
        // Get story data from intent
        Intent intent = getIntent();
        String storyType = intent.getStringExtra("story_type");

        switch (storyType) {
            case "OPENING":
                showOpeningStory();
                break;
            case "WORLD4_COMPLETION": // CHANGED from WORLD5_COMPLETION
                showWorld4CompletionStory();
                break;
            default:
                showOpeningStory();
                break;
        }
    }

    private void showOpeningStory() {
        storyTitleText.setText("Prolog"); // CHANGED from "The Royal Challenge"

        String storyContent = "The great King has gathered all knights of the realm to his throne room.\n\n" +
                "\"Brave warriors,\" the King declares, \"the time has come to prove your worth through combat!\"\n\n" +
                "\"Face the challenges that lie ahead, defeat the enemies of our kingdom, and show your valor in battle.\"\n\n" +
                "\"The knight who proves themselves most worthy shall be granted the ultimate honor...\"\n\n" +
                "\"...to become a LORD of the realm!\"\n\n" +
                "⚔️ Your quest begins now! ⚔️";

        storyContentText.setText(storyContent);
        continueButton.setText("BEGIN THE TRIALS!");
    }

    private void showWorld4CompletionStory() {
        storyTitleText.setText("Proven Worthy");

        String storyContent = "You have conquered four worlds and proven your skill in countless battles!\n\n" +
                "The King nods with approval as he witnesses your incredible achievements.\n\n" +
                "\"You have shown exceptional valor and strength, brave knight!\"\n\n" +
                "\"But to truly earn the title of LORD, you must undergo the ultimate transformation...\"\n\n" +
                "\"Seek the power of CHARACTER DEVELOPMENT and achieve the pinnacle of knightly evolution!\"\n\n" +
                "\"Only when you become the AXOLOTL LORD may you proceed to the first chapter of your true destiny!\"\n\n" +
                "🌟 The path to lordship awaits! 🌟";

        storyContentText.setText(storyContent);
        continueButton.setText("UNDERSTOOD, MY KING!");
    }

    private void handleContinue() {
        Intent intent = getIntent();
        String storyType = intent.getStringExtra("story_type");

        if (storyType.equals("OPENING")) {
            // Continue to GameActivity
            Intent gameIntent = new Intent(StoryActivity.this, GameActivity.class);
            startActivity(gameIntent);
            finish();
        } else if (storyType.equals("WORLD4_COMPLETION")) { // CHANGED from WORLD5_COMPLETION
            // Return to main menu
            finish();
        }
    }
}