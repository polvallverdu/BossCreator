package engineer.pol.bosscreator.models;

import engineer.pol.bosscreator.FightCreator;
import engineer.pol.bosscreator.utils.cmds.CmdCases;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class PlayerFight extends Fight {

    private ServerPlayerEntity player;
    private int health;

    public PlayerFight(String name, FightConfig config) {
        super(name, config, FightType.PLAYER);
        this.resetHealth();
    }

    public void resetHealth() {
        this.health = this.getConfig().getMaxHealth();
    }

    /**
     * @param hp from 0.0 to 1.0, 1.0 being 100% health and 0.0 being 0% health
     */
    public void setHP(float hp) {
        this.health = (int) (this.getConfig().getMaxHealth() * hp);
        this.update();
    }

    public boolean isDead() {
        return this.health <= 0;
    }

    @Override
    public boolean hasReachedBlockPercentage() {
        return this.getHealthPoints() <= FightCreator.FIGHT_MANAGER.getBlockPercentage();
    }

    @Override
    public boolean start() {
        if (this.player == null) return false;
        if (!super.start()) return false;

        this.resetHealth();
        this.update();

        FightCreator.CMD_MANAGER.runCommands(CmdCases.START_PLAYERFIGHT, new ArrayList<>(), "player", this.player.getName().toString());

        return true;
    }

    @Override
    public boolean stop(boolean force) {
        if (!super.stop(force)) return false;

        this.resetHealth();

        FightCreator.CMD_MANAGER.runCommands(force ? CmdCases.FORCE_FINISH_PLAYERFIGHT : CmdCases.FINISH_PLAYERFIGHT, new ArrayList<>(), "player", this.player.getName().toString());

        return true;
    }

    @Override
    public boolean onDamage(int damage) {
        if (!super.onDamage(damage) || this.health <= 0) return false;

        this.health = Math.max(0, this.health - damage);

        this.update();
        if (this.isDead()) {
            //this.resetHealth();
            //this.stop(false);
            FightCreator.CMD_MANAGER.runCommands(CmdCases.FINISH_PLAYERFIGHT, new ArrayList<>(), "player", this.player.getName().toString());
        }

        return true;
    }

    @Override
    public int getHealthPoints() {
        // Percentage from 0-100 respectively of health and config.maxHealth
        return (int) (((double) this.health / (double) this.getConfig().getMaxHealth()) * 100);
    }

    @Override
    public String getBossbarName() {
        return this.getConfig().getDisplayName().replaceAll("\\{player}", this.player == null ? "" : player.getName().getString());
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public int getHealth() {
        return health;
    }
}
