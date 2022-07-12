package engineer.pol.bosscreator.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import engineer.pol.bosscreator.BossCreator;
import engineer.pol.bosscreator.callbacks.EntityDamageCallback;
import engineer.pol.bosscreator.models.BossFight;
import engineer.pol.bosscreator.models.BossTemplate;
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

import java.util.ArrayList;
import java.util.List;

public class BossManager {

    private List<BossTemplate> bossTemplates;
    private List<BossFight> bossFights;

    public BossManager() {
        this.bossTemplates = new ArrayList<>();
        this.bossFights = new ArrayList<>();

        ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(this::onPlayerQuit);

        EntityDamageCallback.EVENT.register(this::onDamage);
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(this::onEntityKilled);

        this.load();
    }

    private void onEntityKilled(ServerWorld world, Entity entity, LivingEntity killedEntity) {
        if (!(entity instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) entity;
        if (this.getActiveBossFights().stream().anyMatch(fight -> fight.hasMorphedPlayer(player.getUuid()))) {
            // TODO: Send kill message that the boss killed the player
        }
    }

    private ActionResult onDamage(LivingEntity receiver, EntityDamageSource source, float amount) {
        if (receiver instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) receiver;

            this.getActiveBossFights().stream()
                    .filter(fight -> fight.getMorphedPlayers().contains(player.getUuid()))
                    .findFirst()
                    .ifPresent(fight -> fight.onDamage((int) amount));
            return ActionResult.FAIL;
        }

        if (source.getAttacker() instanceof ServerPlayerEntity) { // If the attacker is the boss, apply damage to received
            ServerPlayerEntity player = (ServerPlayerEntity) source.getAttacker();

            this.getActiveBossFights().stream()
                    .filter(fight -> fight.getMorphedPlayers().contains(player.getUuid()))
                    .findFirst()
                    .ifPresent(fight -> receiver.damage(new BossDamageSource(), (float) fight.getTemplate().getMeleeDamage()));

            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private void load() {
        JsonObject json = BossCreator.DATA_FILE.load();
        if (!json.has("bosses")) {
            json.add("bosses", new JsonArray());
        }
        json.get("bosses").getAsJsonArray().forEach(bossJson -> {
            BossTemplate template = BossTemplate.fromJson(bossJson.getAsJsonObject());
            this.bossTemplates.add(template);
        });
    }

    public void save() {
        JsonObject json = new JsonObject();
        JsonArray bosses = new JsonArray();
        this.bossTemplates.forEach(template -> {
            bosses.add(template.toJson());
        });
        json.add("bosses", bosses);
        BossCreator.DATA_FILE.save(json);
    }

    public BossTemplate createBossTemplate(String name) {
        if (this.getBossTemplate(name) != null) {
            throw new RuntimeException("Boss template with name '" + name + "' already exists.");
        }

        BossTemplate bossTemplate = new BossTemplate(name);
        this.bossTemplates.add(bossTemplate);
        return bossTemplate;
    }

    public BossTemplate removeBossTemplate(String name) {
        BossTemplate bossTemplate = this.getBossTemplate(name);
        if (bossTemplate == null) {
            throw new RuntimeException("Boss template with name '" + name + "' does not exist.");
        }

        this.bossTemplates.remove(bossTemplate);
        this.save();
        return bossTemplate;
    }

    public BossTemplate getBossTemplate(String name) {
        for (BossTemplate bossTemplate : this.bossTemplates) {
            if (bossTemplate.getName().equals(name)) {
                return bossTemplate;
            }
        }
        return null;
    }

    public BossTemplate deleteBossTemplate(String name) {
        for (BossTemplate bossTemplate : this.bossTemplates) {
            if (bossTemplate.getName().equals(name)) {
                this.bossTemplates.remove(bossTemplate);
                return bossTemplate;
            }
        }
        return null;
    }

    public BossFight createBossFight(String name, BossTemplate bossTemplate) {
        BossFight bossFight = new BossFight(name, bossTemplate);
        this.bossFights.add(bossFight);
        return bossFight;
    }

    public BossFight getBossFight(String name) {
        for (BossFight bossFight : this.bossFights) {
            if (bossFight.getName().equals(name)) {
                return bossFight;
            }
        }
        return null;
    }

    public void onPlayerJoin(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        ServerPlayerEntity serverPlayerEntity = serverPlayNetworkHandler.player;
        for (BossFight bossFight : this.bossFights) {
            if (bossFight.hasMorphedPlayer(serverPlayerEntity.getUuid())) {
                bossFight.removeOfflinePlayer(serverPlayerEntity);
            }
            if (bossFight.isRunning()) {
                bossFight.addBossbarPlayer(serverPlayerEntity);
            }
        }
    }

    public void onPlayerQuit(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer minecraftServer) {
        ServerPlayerEntity serverPlayerEntity = serverPlayNetworkHandler.player;
        for (BossFight bossFight : this.bossFights) {
            if (bossFight.isRunning()) {
                bossFight.removeMorpedPlayer(serverPlayerEntity.getUuid(), true);
            }
        }
    }

    public List<BossTemplate> getBossTemplates() {
        return bossTemplates;
    }

    public List<BossFight> getBossFights() {
        return bossFights;
    }

    public List<BossFight> getActiveBossFights() {
        return this.bossFights.stream().filter(BossFight::isRunning).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }


}
