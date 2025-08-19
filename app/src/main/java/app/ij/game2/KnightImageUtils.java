package app.ij.game2;

import android.content.Context;

public class KnightImageUtils {

    /**
     * Gets the appropriate image resource for a knight
     * @param context Android context
     * @param knightName Name of the knight
     * @param isAttack true for attack image, false for idle/default image
     * @return Resource ID for the image
     */
    public static int getKnightImageResource(Context context, String knightName, boolean isAttack) {
        // Handle special knights first
        if (knightName.equals("King's Guard")) {
            // Admin knight uses default images
            if (isAttack) {
                return R.drawable.player_attack;
            } else {
                return R.drawable.player_character;
            }
        }

        // Handle evolved knights - remove "Evolved " prefix for image lookup
        String imageKnightName = knightName;
        if (knightName.startsWith("Evolved ")) {
            imageKnightName = knightName.replace("Evolved ", "");
        }

        // Convert knight name to resource-friendly format
        String resourceName = imageKnightName.toLowerCase()
                .replace(" ", "_")
                .replace("'", "")
                .replace("-", "_");

        if (isAttack) {
            resourceName += "_attack";
        } else {
            resourceName += "_idle";
        }

        // Try to get the specific knight image
        int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());

        if (resourceId != 0) {
            // Knight-specific image exists
            android.util.Log.d("KnightImages", "Using specific image: " + resourceName + " for knight: " + knightName);
            return resourceId;
        } else {
            // Fall back to default images
            android.util.Log.d("KnightImages", "Using default image for: " + knightName + " (tried: " + resourceName + ")");
            if (isAttack) {
                return R.drawable.player_attack;
            } else {
                return R.drawable.player_character;
            }
        }
    }

    /**
     * Gets the idle/default image for a knight (most common use case)
     * @param context Android context
     * @param knightName Name of the knight
     * @return Resource ID for the idle image
     */
    public static int getKnightIdleImage(Context context, String knightName) {
        return getKnightImageResource(context, knightName, false);
    }

    /**
     * Gets the attack image for a knight
     * @param context Android context
     * @param knightName Name of the knight
     * @return Resource ID for the attack image
     */
    public static int getKnightAttackImage(Context context, String knightName) {
        return getKnightImageResource(context, knightName, true);
    }

    /**
     * Debug method to check what images would be loaded for a knight
     * @param context Android context
     * @param knightName Name of the knight
     */
    public static void debugKnightImages(Context context, String knightName) {
        android.util.Log.d("KnightImageDebug", "=== Checking images for: " + knightName + " ===");

        String imageKnightName = knightName;
        if (knightName.startsWith("Evolved ")) {
            imageKnightName = knightName.replace("Evolved ", "");
            android.util.Log.d("KnightImageDebug", "Evolved knight detected, using base name: " + imageKnightName);
        }

        String idleResourceName = imageKnightName.toLowerCase()
                .replace(" ", "_")
                .replace("'", "")
                .replace("-", "_") + "_idle";

        String attackResourceName = imageKnightName.toLowerCase()
                .replace(" ", "_")
                .replace("'", "")
                .replace("-", "_") + "_attack";

        int idleId = context.getResources().getIdentifier(idleResourceName, "drawable", context.getPackageName());
        int attackId = context.getResources().getIdentifier(attackResourceName, "drawable", context.getPackageName());

        android.util.Log.d("KnightImageDebug", "Idle: " + idleResourceName + " -> " + (idleId != 0 ? "FOUND" : "NOT FOUND"));
        android.util.Log.d("KnightImageDebug", "Attack: " + attackResourceName + " -> " + (attackId != 0 ? "FOUND" : "NOT FOUND"));
    }
}