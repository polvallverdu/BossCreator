package engineer.pol.bosscreator.models;

import engineer.pol.bosscreator.FightCreator;
import engineer.pol.bosscreator.utils.cmds.CmdCases;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;

public abstract class Fight {

    private final String name;
    private boolean running;
    private ServerBossBar bossBar = null;
    private FightConfig config;
    private final FightType type;

    public Fight(String name, FightConfig config, FightType type) {
        this.name = name;
        this.running = false;

        this.config = config;

        this.type = type;
    }

    /**
     *
     * @return a percentage from 0-100
     */
    public abstract int getHealthPoints();

    public String getName() {
        return name;
    }

    public boolean isRunning() {
        return running;
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        if (this.bossBar != null) {
            this.bossBar.addPlayer(player);
        }
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        // Nothing implemented
    }

    public boolean hasReachedBlockPercentage() {
        return false;
    };

    public boolean onDamage(int damage) {
        if (!this.running || FightCreator.FIGHT_MANAGER.isBlocked()) return false;
        this.update();
        if (this.hasReachedBlockPercentage() && FightCreator.FIGHT_MANAGER.isBlockEnabled()) {
            FightCreator.FIGHT_MANAGER.setBlocked(true);
        }
        return true;
    }

    private void createBossbar() {
        if (this.bossBar == null) {
            this.bossBar = new ServerBossBar(Text.literal(this.getBossbarName()), this.config.getColor(), BossBar.Style.PROGRESS);
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
        FightCreator.SERVER.getPlayerManager().getPlayerList().forEach(this::onPlayerJoin);
    }

    public boolean start() {
        if (this.running) return false;
        this.running = true;

        createBossbar();
        update();

        FightCreator.CMD_MANAGER.runCommands(CmdCases.START, new ArrayList<>());

        return true;
    }

    public boolean stop(boolean force) {
        if (!this.running) return false;
        this.running = false;

        destroyBossbar();
        FightCreator.CMD_MANAGER.runCommands(force ? CmdCases.FORCE_FINISH : CmdCases.FINISH, new ArrayList<>());

        return true;
    }

    public abstract String getBossbarName();

    private String getReplacedBossbarName() {
        return this.getBossbarName().replace("\\{fight}", this.getName());
    }

    protected void update() {
        if (this.bossBar == null) return;
        this.bossBar.setColor(this.config.getColor());
        this.bossBar.setName(Text.literal(this.getReplacedBossbarName()));

        int healthPoints = this.getHealthPoints();
        if (healthPoints < 0) {
            this.bossBar.setPercent(0);
        } else if (healthPoints > 100) {
            this.bossBar.setPercent(1);
        } else {
            this.bossBar.setPercent((float) (healthPoints/100.0));
        }
    }

    public FightConfig getConfig() {
        return config;
    }

    public FightType getType() {
        return type;
    }
}
