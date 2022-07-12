package engineer.pol.bosscreator.models;

import engineer.pol.bosscreator.BossCreator;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BossFight {

    private final String name;
    private final BossTemplate template;
    private int health;
    private float healthPoints; // from 0.0 to 1.0, 1.0 being 100% health and 0.0 being 0% health
    private boolean running;
    private final List<UUID> morphedPlayers;
    private final List<UUID> removePlayers;

    private ServerBossBar bossBar = null;

    public BossFight(String name, BossTemplate template) {
        this.name = name;
        this.template = template;
        this.health = template.getMaxHealth();
        this.calculateHealthPoints();
        this.running = false;
        this.morphedPlayers = new ArrayList<>();
        this.removePlayers = new ArrayList<>();
    }

    private void calculateHealthPoints() {
        this.healthPoints = (float) this.health / (float) this.template.getMaxHealth();
    }

    public String getName() {
        return name;
    }

    public BossTemplate getTemplate() {
        return template;
    }

    public int getHealth() {
        return health;
    }

    public float getHealthPoints() {
        return healthPoints;
    }

    public boolean isRunning() {
        return running;
    }

    public void addMorphedPlayer(UUID player) {
        if (this.morphedPlayers.contains(player)) return;

        this.morphedPlayers.add(player);
        if (this.running) {
            this.disguise(BossCreator.SERVER.getPlayerManager().getPlayer(player));
        }
    }

    public void addBossbarPlayer(ServerPlayerEntity player) {
        this.bossBar.addPlayer(player);
    }

    public void removeMorpedPlayer(UUID player) {
        this.removeMorpedPlayer(player, false);
    }

    public void removeMorpedPlayer(UUID player, boolean offline) {
        if (!this.morphedPlayers.remove(player)) return;

        if (offline) {
            this.removePlayers.add(player);
        } else if (this.running) {
            this.undisguise(BossCreator.SERVER.getPlayerManager().getPlayer(player));
        }
    }

    public boolean hasMorphedPlayer(UUID player) {
        return this.morphedPlayers.contains(player);
    }

    public boolean hasOfflinePlayer(UUID player) {
        return this.removePlayers.contains(player);
    }

    public void removeOfflinePlayer(ServerPlayerEntity player) {
        this.removePlayers.remove(player.getUuid());
        this.undisguise(player);
    }

    public List<UUID> getMorphedPlayers() {
        return this.morphedPlayers;
    }

    public List<UUID> getRemovePlayers() {
        return removePlayers;
    }

    /**
     * @param hp from 0.0 to 1.0, 1.0 being 100% health and 0.0 being 0% health
     */
    public void setHP(float hp) {
        this.healthPoints = hp;
        this.health = (int) (this.template.getMaxHealth() * this.healthPoints);
        update();
    }

    public void start() {
        if (this.running) return;
        this.running = true;
        this.health = this.template.getMaxHealth();
        createBossbar();
        update();

        this.getMorphedPlayersEntites().forEach(this::disguise);
    }

    private List<ServerPlayerEntity> getMorphedPlayersEntites() {
        return this.morphedPlayers.stream().map(BossCreator.SERVER.getPlayerManager()::getPlayer).filter(Objects::nonNull).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void stop() {
        if (!this.running) return;
        this.running = false;
        destroyBossbar();

        this.getMorphedPlayersEntites().forEach(this::undisguise);
    }

    public void onDamage(int damage) {
        if (!this.running) return;

        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            // TODO: DEATH
            this.stop();
        }

        this.update();
    }

    private void createBossbar() {
        if (this.bossBar == null) {
            this.bossBar = new ServerBossBar(Text.literal(this.template.getDisplayName()), this.template.getColor(), BossBar.Style.PROGRESS);
        }
        this.bossBar.setVisible(false);
        this.addAllPlayersToBossbar();
        this.update();
        this.bossBar.setVisible(true);
    }

    private void destroyBossbar() {
        if (this.bossBar == null) return;
        this.bossBar.setVisible(false);
    }

    private void addAllPlayersToBossbar() {
        this.getMorphedPlayers().stream()
                .map(playerUUID -> BossCreator.SERVER.getPlayerManager().getPlayer(playerUUID))
                .filter(Objects::nonNull)
                .forEach(this::addBossbarPlayer);
    }

    private void update() {
        if (this.bossBar == null) return;
        this.bossBar.setColor(this.template.getColor());
        this.bossBar.setName(Text.literal(this.template.getDisplayName()));
        this.calculateHealthPoints();
        this.bossBar.setPercent(this.healthPoints);
    }

    private void disguise(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.SURVIVAL);
        BossCreator.SERVER.getCommandManager().execute(BossCreator.SERVER.getCommandSource(), "morpher " + player.getName() + " model " + this.template.getMorpherModel());
        BossCreator.SERVER.getCommandManager().execute(BossCreator.SERVER.getCommandSource(), "morpher " + player.getName() + " texture " + this.template.getMorpherTexture());
        BossCreator.SERVER.getCommandManager().execute(BossCreator.SERVER.getCommandSource(), "morpher " + player.getName() + " animation " + this.template.getMorpherAnimation());
    }

    private void undisguise(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.SPECTATOR);
        BossCreator.SERVER.getCommandManager().execute(BossCreator.SERVER.getCommandSource(), "morpher " + player.getName() + " model models/player.geo.json");
        BossCreator.SERVER.getCommandManager().execute(BossCreator.SERVER.getCommandSource(), "morpher " + player.getName() + " texture reset");
        BossCreator.SERVER.getCommandManager().execute(BossCreator.SERVER.getCommandSource(), "morpher " + player.getName() + " animation animations/player.animation.json");
    }
}
