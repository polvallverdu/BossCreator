package engineer.pol.bosscreator.utils;

import net.minecraft.entity.boss.BossBar;

import java.util.List;

public class BossBarUtils {

    public static BossBar.Color getColorFromString(String colorName) { // colorname might not be case sensitive
        if (colorName == null) return null;

        switch (colorName.toLowerCase()) {
            case "red":
                return BossBar.Color.RED;
            case "green":
                return BossBar.Color.GREEN;
            case "blue":
                return BossBar.Color.BLUE;
            case "white":
                return BossBar.Color.WHITE;
            case "purple":
                return BossBar.Color.PURPLE;
            case "yellow":
                return BossBar.Color.YELLOW;
            case "pink":
                return BossBar.Color.PINK;
        }
        return null;
    }

    public static List<String> getBossbarColors() {
        return List.of("red", "green", "blue", "white", "purple", "yellow", "pink");
    }

}
