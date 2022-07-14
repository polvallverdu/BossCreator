package engineer.pol.bosscreator.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import engineer.pol.bosscreator.FightCreator;
import engineer.pol.bosscreator.callbacks.EntityDamageCallback;
import engineer.pol.bosscreator.file.DataFile;
import engineer.pol.bosscreator.models.*;
import engineer.pol.bosscreator.utils.cmds.CmdCases;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class FightManager {

    private DataFile dataFile;

    private List<FightConfig> fightConfigs;
    private List<Fight> fights;

    private boolean knockback;
    private int blockPercentage; // Goes from 0-100
    private boolean blockEnabled;
    private boolean blocked;

    public FightManager() {
        this.dataFile = new DataFile("bosses");

        this.fightConfigs = new ArrayList<>();
        this.fights = new ArrayList<>();

        this.knockback = false;

        ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(this::onPlayerQuit);

        EntityDamageCallback.EVENT.register(this::onDamage);
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(this::onEntityKilled);

        blockPercentage = 50;
        blockEnabled = false;
        blocked = false;

        this.load();
    }

    private void onEntityKilled(ServerWorld world, Entity entity, LivingEntity killedEntity) {
        if (!(entity instanceof ServerPlayerEntity) || !(killedEntity instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) entity;
        this.getActiveFights(FightType.BOSSFIGHT).stream().map(f -> (BossFight) f)
                .filter(fight -> ((BossFight) fight).hasMorphedPlayer(player.getUuid()))
                .findFirst()
                .ifPresent(fight -> FightCreator.CMD_MANAGER.runCommands(CmdCases.BOSSFIGHT_BOSS_KILL, fight.getMorphedPlayersEntity(), "player", killedEntity.getName().getString() ,"boss", fight.getConfig().getName()));
    }

    private Pair<ActionResult, Float> onDamage(LivingEntity receiver, EntityDamageSource source, float amount) {
        AtomicReference<ActionResult> result = new AtomicReference<>(ActionResult.PASS);
        AtomicReference<Float> damage = new AtomicReference<>(amount);

        if (receiver instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) receiver;

            this.getActiveFights(FightType.BOSSFIGHT).stream().map(f -> (BossFight) f)
                    .filter(fight -> fight.getMorphedPlayers().contains(player.getUuid()))
                    .findFirst()
                    .ifPresent(fight -> {
                        fight.onDamage((int) amount);
                        damage.set(0.1f);
                        result.set(ActionResult.SUCCESS);
                    });
        }

        if (source.getAttacker() instanceof ServerPlayerEntity) { // If the attacker is the boss, apply damage to received
            ServerPlayerEntity player = (ServerPlayerEntity) source.getAttacker();

            List<Fight> activeFights = this.getActiveFights(null);

            activeFights.stream().filter(f -> f instanceof BossFight).map(f -> (BossFight) f)
                .filter(fight -> fight.getMorphedPlayers().contains(player.getUuid()))
                .findFirst()
                .ifPresent(fight -> {
                    damage.set(source.isProjectile() ? (float) fight.getConfig().getProjectileDamage() : (float) fight.getConfig().getMeleeDamage());
                    result.set(ActionResult.SUCCESS);
                });

            activeFights.stream().filter(f -> f instanceof PlayerFight).map(f -> (PlayerFight) f)
                    .filter(f -> f.getPlayer() != null && f.getPlayer().getUuid().equals(player.getUuid()))
                    .findFirst()
                    .ifPresent(f -> {
                        f.onDamage((int) amount);
                    });
        }

        if (damage.get() == -1f) {
            damage.set(amount);
        }

        return new Pair<>(result.get(), damage.get());
    }

    private void load() {
        JsonObject json = this.dataFile.load();
        if (!json.has("bosses")) {
            json.add("bosses", new JsonArray());
        }
        json.get("bosses").getAsJsonArray().forEach(bossJson -> {
            FightConfig template = FightConfig.fromJson(bossJson.getAsJsonObject());
            this.fightConfigs.add(template);
        });
        if (!json.has("knockback")) {
            json.addProperty("knockback", this.knockback);
        }
        if (!json.has("blockPercentage")) {
            json.addProperty("blockPercentage", this.blockPercentage);
        }
        if (!json.has("blockEnabled")) {
            json.addProperty("blockEnabled", this.blockEnabled);
        }
        if (!json.has("blocked")) {
            json.addProperty("blocked", this.blocked);
        }
        this.knockback = json.get("knockback").getAsBoolean();
        this.blockPercentage = json.get("blockPercentage").getAsInt();
        this.blockEnabled = json.get("blockEnabled").getAsBoolean();
        this.blocked = json.get("blocked").getAsBoolean();
    }

    public void save() {
        JsonObject json = new JsonObject();
        JsonArray bosses = new JsonArray();
        this.fightConfigs.forEach(template -> {
            bosses.add(template.toJson());
        });
        json.add("bosses", bosses);

        json.addProperty("knockback", this.knockback);
        json.addProperty("blockPercentage", this.blockPercentage);
        json.addProperty("blockEnabled", this.blockEnabled);
        json.addProperty("blocked", this.blocked);

        this.dataFile.save(json);
    }

    public FightConfig createBossTemplate(String name) {
        if (this.getFightConfig(name) != null) {
            throw new RuntimeException("Boss template with name '" + name + "' already exists.");
        }

        FightConfig fightConfig = new FightConfig(name);
        this.fightConfigs.add(fightConfig);
        return fightConfig;
    }

    public FightConfig removeBossTemplate(String name) {
        FightConfig fightConfig = this.getFightConfig(name);
        if (fightConfig == null) {
            throw new RuntimeException("Boss template with name '" + name + "' does not exist.");
        }

        this.fightConfigs.remove(fightConfig);
        this.save();
        return fightConfig;
    }

    public FightConfig getFightConfig(String name) {
        for (FightConfig fightConfig : this.fightConfigs) {
            if (fightConfig.getName().equals(name)) {
                return fightConfig;
            }
        }
        return null;
    }

    public FightConfig deleteBossTemplate(String name) {
        for (FightConfig fightConfig : this.fightConfigs) {
            if (fightConfig.getName().equals(name)) {
                this.fightConfigs.remove(fightConfig);
                return fightConfig;
            }
        }
        return null;
    }

    public BossFight createBossFight(String name, FightConfig fightConfig) {
        // Check if bossfight with name already exists
        if (this.getFight(name) != null) {
            throw new RuntimeException("A fight with name '" + name + "' already exists.");
        }
        BossFight bossFight = new BossFight(name, fightConfig);
        this.fights.add(bossFight);
        return bossFight;
    }

    public PlayerFight createPlayerFight(String name, FightConfig fightConfig) {
        // Check if bossfight with name already exists
        if (this.getFight(name) != null) {
            throw new RuntimeException("A fight with name '" + name + "' already exists.");
        }
        PlayerFight bossFight = new PlayerFight(name, fightConfig);
        this.fights.add(bossFight);
        return bossFight;
    }

    public Fight getFight(String name) {
        for (Fight fight : this.fights) {
            if (fight.getName().equals(name)) {
                return fight;
            }
        }
        return null;
    }

    public BossFight getBossFight(String name) {
        Fight fight = this.getFight(name);
        return !(fight instanceof BossFight) ? null : (BossFight) fight;
    }

    public PlayerFight getPlayerFight(String name) {
        Fight fight = this.getFight(name);
        return !(fight instanceof PlayerFight) ? null : (PlayerFight) fight;
    }

    public void onPlayerJoin(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        ServerPlayerEntity serverPlayerEntity = serverPlayNetworkHandler.player;
        for (Fight fight : this.fights) {
            fight.onPlayerJoin(serverPlayerEntity);
        }
    }

    public void onPlayerQuit(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer minecraftServer) {
        ServerPlayerEntity serverPlayerEntity = serverPlayNetworkHandler.player;
        for (Fight fight : this.fights) {
            fight.onPlayerLeave(serverPlayerEntity);
        }
    }

    public List<FightConfig> getBossTemplates() {
        return fightConfigs;
    }

    public List<Fight> getFights() {
        return fights;
    }

    public List<Fight> getFights(FightType filter) {
        return fights.stream().filter(fight -> fight.getType() == filter).collect(Collectors.toList());
    }

    public List<Fight> getActiveFights(@Nullable FightType filter) {
        return this.fights.stream().filter(Fight::isRunning).filter(f -> filter == null || f.getType() == filter).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public boolean isKnockback() {
        return knockback;
    }

    public void setKnockback(boolean knockback) {
        this.knockback = knockback;
    }

    public int getBlockPercentage() {
        return blockPercentage;
    }

    public void setBlockPercentage(int blockPercentage) {
        this.blockPercentage = blockPercentage;
    }

    public boolean isBlockEnabled() {
        return blockEnabled;
    }

    public void setBlockEnabled(boolean blockEnabled) {
        this.blockEnabled = blockEnabled;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
        FightCreator.CMD_MANAGER.runCommands(blocked ? CmdCases.BOSSBAR_BLOCK : CmdCases.BOSSBAR_UNBLOCK, new ArrayList<>());

    }
}
