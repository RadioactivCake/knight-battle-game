package app.ij.game2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

public class BattleImageUtils {

    public static Drawable getKnightBattleImage(Context context, String knightName, boolean isAttacking) {
        String suffix = isAttacking ? "_attack" : "_idle";
        String imageName = knightName.toLowerCase().replace(" ", "_") + suffix;

        // Try to get specific knight image
        int resourceId = getImageResourceId(context, imageName);
        if (resourceId != 0) {
            return ContextCompat.getDrawable(context, resourceId);
        }

        // Fallback to default knight image
        String defaultName = "knight_default" + suffix;
        resourceId = getImageResourceId(context, defaultName);
        if (resourceId != 0) {
            return ContextCompat.getDrawable(context, resourceId);
        }

        // Final fallback to player character
        return ContextCompat.getDrawable(context, R.drawable.player_character);
    }

    public static Drawable getEnemyBattleImage(Context context, String enemyName, boolean isAttacking) {
        String suffix = isAttacking ? "_attack" : "_idle";
        String imageName = enemyName.toLowerCase().replace(" ", "_") + suffix;

        // Try to get specific enemy image
        int resourceId = getImageResourceId(context, imageName);
        if (resourceId != 0) {
            return ContextCompat.getDrawable(context, resourceId);
        }

        // Fallback to default enemy image
        String defaultName = "enemy_default" + suffix;
        resourceId = getImageResourceId(context, defaultName);
        if (resourceId != 0) {
            return ContextCompat.getDrawable(context, resourceId);
        }

        // Final fallback - create simple colored drawable
        return ContextCompat.getDrawable(context, android.R.color.holo_red_dark);
    }

    private static int getImageResourceId(Context context, String imageName) {
        return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
    }
}