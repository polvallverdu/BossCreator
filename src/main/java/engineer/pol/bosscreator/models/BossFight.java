package engineer.pol.bosscreator.models;

import engineer.pol.bosscreator.FightCreator;
import engineer.pol.bosscreator.utils.cmds.CmdCases;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BossFight extends Fight {

    private int health;
    private final List<UUID> morphedPlayers;
    private final List<UUID> toRemovePlayers;

    public BossFight(String name, FightConfig config) {
        super(name, config, FightType.BOSSFIGHT);
        this.resetHealth();
        this.morphedPlayers = new ArrayList<>();
        this.toRemovePlayers = new ArrayList<>();
    }

    public void resetHealth() {
        this.health = getConfig().getMaxHealth();
    }

    @Override
    public int getHealthPoints() {
        // Percentage from 0-100 respectively of health and config.getMaxHealth()
        return (int) (((double) this.health / (double) this.getConfig().getMaxHealth()) * 100);
    }

    public int getHealth() {
        return health;
    }

    public void addMorphedPlayer(UUID player) {
        if (this.morphedPlayers.contains(player)) return;

        this.morphedPlayers.add(player);
        if (this.isRunning()) {
            this.disguise(FightCreator.SERVER.getPlayerManager().getPlayer(player));
        }
    }

    public void removeMorphedPlayer(UUID player) {
        this.removeMorphedPlayer(player, false);
    }

    public void removeMorphedPlayer(UUID player, boolean offline) {
        if (!this.morphedPlayers.remove(player)) return;

        if (offline) {
            this.toRemovePlayers.add(player);
        } else if (this.isRunning()) {
            this.undisguise(FightCreator.SERVER.getPlayerManager().getPlayer(player));
        }
    }

    public boolean hasMorphedPlayer(UUID player) {
        return this.morphedPlayers.contains(player);
    }

    public boolean hasOfflinePlayer(UUID player) {
        return this.toRemovePlayers.contains(player);
    }

    public void removeOfflinePlayer(ServerPlayerEntity player) {
        this.toRemovePlayers.remove(player.getUuid());
        this.undisguise(player);
    }

    public List<UUID> getMorphedPlayers() {
        return this.morphedPlayers;
    }

    public List<UUID> getRemovePlayers() {
        return toRemovePlayers;
    }

    /**
     * @param hp from 0.0 to 1.0, 1.0 being 100% health and 0.0 being 0% health
     */
    public void setHP(float hp) {
        this.health = (int) (this.getConfig().getMaxHealth() * hp);
        this.update();
    }

    @Override
    public boolean start() {
        if (!super.start()) return false;

        FightCreator.CMD_MANAGER.runCommands(CmdCases.START_BOSSFIGHT, this.getMorphedPlayersEntity(), "boss", this.getConfig().getName());
        this.getMorphedPlayersEntity().forEach(this::disguise);
        return true;
    }

    public List<ServerPlayerEntity> getMorphedPlayersEntity() {
        return this.morphedPlayers.stream().map(FightCreator.SERVER.getPlayerManager()::getPlayer).filter(Objects::nonNull).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public boolean stop(boolean force) {
        if (!super.stop(force)) return false;

        FightCreator.CMD_MANAGER.runCommands(force ? CmdCases.FORCE_FINISH_BOSSFIGHT : CmdCases.FINISH_BOSSFIGHT, this.getMorphedPlayersEntity(), "boss", this.getConfig().getName());
        this.getMorphedPlayersEntity().forEach(this::undisguise);
        return true;
    }

    @Override
    public String getBossbarName() {
        return this.getConfig().getDisplayName();
    }

    @Override
    public boolean hasReachedBlockPercentage() {
        return this.getHealthPoints() <= FightCreator.FIGHT_MANAGER.getBlockPercentage();
    }

    @Override
    public boolean onDamage(int damage) {
        if (!super.onDamage(damage) || this.health <= 0) return false;

        this.health = Math.max(0, this.health - damage);

        this.update();
        if (this.isDead()) {
            //resetHealth();
            FightCreator.CMD_MANAGER.runCommands(CmdCases.BOSSFIGHT_BOSS_KILL, this.getMorphedPlayersEntity(), "boss", this.getConfig().getName());
            //this.stop(false);
            FightCreator.CMD_MANAGER.runCommands(CmdCases.FINISH_BOSSFIGHT, this.getMorphedPlayersEntity(), "boss", this.getConfig().getName());
        }

        return true;
    }

    public boolean isDead() {
        return this.health <= 0;
    }

    private void disguise(ServerPlayerEntity player) {
        FightCreator.CMD_MANAGER.runCommands(CmdCases.MORPHED_PLAYER_ADD, this.getMorphedPlayersEntity(), "player", player.getName().getString(), "boss", this.getConfig().getName());
    }

    private void undisguise(ServerPlayerEntity player) {
        FightCreator.CMD_MANAGER.runCommands(CmdCases.MORPHED_PLAYER_REMOVE, this.getMorphedPlayersEntity(), "player", player.getName().getString(), "boss", this.getConfig().getName());
    }
}
