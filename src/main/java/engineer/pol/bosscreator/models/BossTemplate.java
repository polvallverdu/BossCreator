package engineer.pol.bosscreator.models;

import com.google.gson.JsonObject;
import net.minecraft.entity.boss.BossBar;

public class BossTemplate {

    private final String name;
    private String displayName;
    private int maxHealth;
    private int meleeDamage;
    private BossBar.Color color;

    private String morpherModel;
    private String morpherTexture;
    private String morpherAnimation;

    public BossTemplate(String name) {
        this(name, name, 100, 2, BossBar.Color.WHITE);
    }

    public BossTemplate(String name, String displayName, int maxHealth, int meleeDamage, BossBar.Color color) {
        this(name, displayName, maxHealth, meleeDamage, color, "models/player.geo.json", "reset", "animations/player.animation.json");
    }

    public BossTemplate(String name, String displayName, int maxHealth, int meleeDamage, BossBar.Color color, String morpherModel, String morpherTexture, String morpherAnimation) {
        this.name = name;
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.meleeDamage = meleeDamage;
        this.color = color;

        this.morpherModel = morpherModel;
        this.morpherTexture = morpherTexture;
        this.morpherAnimation = morpherAnimation;
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

    public String getMorpherModel() {
        return morpherModel;
    }

    public String getMorpherTexture() {
        return morpherTexture;
    }

    public String getMorpherAnimation() {
        return morpherAnimation;
    }

    public void setMorpherModel(String morpherModel) {
        this.morpherModel = morpherModel;
    }

    public void setMorpherTexture(String morpherTexture) {
        this.morpherTexture = morpherTexture;
    }

    public void setMorpherAnimation(String morpherAnimation) {
        this.morpherAnimation = morpherAnimation;
    }

    public int getMeleeDamage() {
        return meleeDamage;
    }

    public void setMeleeDamage(int meleeDamage) {
        this.meleeDamage = meleeDamage;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        json.addProperty("name", name);
        json.addProperty("displayName", displayName);
        json.addProperty("maxHealth", maxHealth);
        json.addProperty("meleeDamage", meleeDamage);
        json.addProperty("color", color.name());
        json.addProperty("morpherModel", morpherModel);
        json.addProperty("morpherTexture", morpherTexture);
        json.addProperty("morpherAnimation", morpherAnimation);

        return json;
    }

    public static BossTemplate fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        String displayName = json.get("displayName").getAsString();
        int maxHealth = json.get("maxHealth").getAsInt();
        int meleeDamage = json.get("meleeDamage").getAsInt();
        BossBar.Color color = BossBar.Color.valueOf(json.get("color").getAsString());
        String morpherModel = json.get("morpherModel").getAsString();
        String morpherTexture = json.get("morpherTexture").getAsString();
        String morpherAnimation = json.get("morpherAnimation").getAsString();

        return new BossTemplate(name, displayName, maxHealth, meleeDamage, color, morpherModel, morpherTexture, morpherAnimation);
    }
}
