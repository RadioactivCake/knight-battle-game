// ================================
// COMPLETE REPLACEMENT: EventDatabase.java
// ================================

package app.ij.game2;

import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventDatabase {

    private static final List<Event> AVAILABLE_EVENTS = new ArrayList<>();

    static {
        // Initialize all available events
        AVAILABLE_EVENTS.add(new Event(
                "Life Tree",
                "A mystical tree restores all your lost health!",
                "COMMON",
                99.0f  // 99% chance when event triggers (when King's Blessing is possible)
        ));

        AVAILABLE_EVENTS.add(new Event(
                "King's Blessing",
                "The ancient king grants you his blessing! (Unlocks dual squires OR +100 coins if already unlocked)",
                "LEGENDARY",
                10.0f   // 10% chance
        ));
    }

    // NEW METHOD: Get event based on stage and unlock status
    // NEW METHOD: Get event based on stage and unlock status
    public static Event getEventForStage(int currentStage, SharedPreferences sharedPreferences) {
        // Stage 5+ (mini boss completed) = always Life Tree
        if (currentStage >= 5) {
            return getEventByName("Life Tree");
        }

        // Stages 1-4: Check if King's Blessing is already unlocked
        boolean hasKingsBlessing = sharedPreferences.getBoolean("has_kings_blessing", false);

        if (hasKingsBlessing) {
            // Already have King's Blessing - but can still get it for coins!
            Random random = new Random();
            if (random.nextInt(100) < 90) { // 10% chance for King's Blessing (for coins)
                return getEventByName("King's Blessing");
            } else {
                return getEventByName("Life Tree"); // 90% chance for Life Tree
            }
        } else {
            // Don't have King's Blessing yet - higher chance to get it
            Random random = new Random();
            if (random.nextInt(100) < 10) { // 10% chance for King's Blessing (to unlock)
                return getEventByName("King's Blessing");
            } else {
                return getEventByName("Life Tree"); // 90% chance for Life Tree
            }
        }
    }

    // NEW METHOD: Get specific event by name
    public static Event getEventByName(String eventName) {
        for (Event event : AVAILABLE_EVENTS) {
            if (event.getName().equals(eventName)) {
                return event;
            }
        }
        return AVAILABLE_EVENTS.get(0); // Fallback to Life Tree
    }

    // UPDATED: Check if event should occur based on stage
    public static boolean shouldEventOccur(int currentStage) {
        if (currentStage >= 5) {
            // After mini boss (stage 5+), always 100% chance for Life Tree
            return true;
        }

        // Progressive chances starting from stage 1
        Random random = new Random();
        float eventChance;

        switch (currentStage) {
            case 1:
                eventChance = 0.20f; // 20% chance after stage 1
                break;
            case 2:
                eventChance = 0.40f; // 40% chance after stage 2
                break;
            case 3:
                eventChance = 0.60f; // 60% chance after stage 3
                break;
            case 4:
                eventChance = 0.80f; // 80% chance after stage 4
                break;
            default:
                eventChance = 0.0f;
                break;
        }

        return random.nextFloat() < eventChance;
    }

    // DEPRECATED: Keep for backwards compatibility, but use getEventForStage() instead
    public static Event getRandomEvent() {
        // This method is now deprecated - use getEventForStage() instead
        return AVAILABLE_EVENTS.get(0); // Fallback to Life Tree
    }

    public static List<Event> getAllEvents() {
        return new ArrayList<>(AVAILABLE_EVENTS);
    }
}