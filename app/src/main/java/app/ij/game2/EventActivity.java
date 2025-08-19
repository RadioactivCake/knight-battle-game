package app.ij.game2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EventActivity extends AppCompatActivity {

    private TextView eventNameText, eventDescriptionText;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide navigation bar and make fullscreen
        hideSystemUI();

        setContentView(R.layout.activity_event);

        initializeUI();
        loadEventData();
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
        eventNameText = findViewById(R.id.eventNameText);
        eventDescriptionText = findViewById(R.id.eventDescriptionText);
        continueButton = findViewById(R.id.continueButton);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to GameActivity
                finish();
            }
        });
    }

    private void loadEventData() {
        // Get event data from intent
        Intent intent = getIntent();
        String eventName = intent.getStringExtra("event_name");
        String eventDescription = intent.getStringExtra("event_description");

        eventNameText.setText(eventName);
        eventDescriptionText.setText(eventDescription);
    }
}