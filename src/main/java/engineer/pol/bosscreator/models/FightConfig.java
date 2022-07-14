package engineer.pol.bosscreator.models;

import com.google.gson.JsonObject;
import net.minecraft.entity.boss.BossBar;

public class FightConfig {

    private final String name;
    private String displayName;
    private int maxHealth;
    private int meleeDamage;
    private int projectileDamage;
    private BossBar.Color color;

    public FightConfig(String name) {
        this(name, name, 100, -1, -1, BossBar.Color.WHITE);
    }

    public FightConfig(String name, String displayName, int maxHealth, int meleeDamage, int projectileDamage, BossBar.Color color) {
        this.name = name;
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.meleeDamage = meleeDamage;
        this.projectileDamage = projectileDamage;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public BossBar.Color getColor() {
        return color;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setColor(BossBar.Color color) {
        this.color = color;
    }

    public int getMeleeDamage() {
        return meleeDamage;
    }

    public void setMeleeDamage(int meleeDamage) {
        this.meleeDamage = meleeDamage;
    }

    public int getProjectileDamage() {
        return projectileDamage;
    }

    public void setProjectileDamage(int projectileDamage) {
        this.projectileDamage = projectileDamage;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        json.addProperty("name", name);
        json.addProperty("displayName", displayName);
        json.addProperty("maxHealth", maxHealth);
        json.addProperty("meleeDamage", meleeDamage);
        json.addProperty("projectileDamage", projectileDamage);
        json.addProperty("color", color.name());

        return json;
    }

    public static FightConfig fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        String displayName = json.get("displayName").getAsString();
        int maxHealth = json.get("maxHealth").getAsInt();
        int meleeDamage = json.get("meleeDamage").getAsInt();
        int projectileDamage = json.get("projectileDamage").getAsInt();
        BossBar.Color color = BossBar.Color.valueOf(json.get("color").getAsString());

        return new FightConfig(name, displayName, maxHealth, meleeDamage, projectileDamage, color);
    }
}
