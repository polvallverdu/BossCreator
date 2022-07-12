package engineer.pol.bosscreator;

import engineer.pol.bosscreator.commands.BossCreatorCommand;
import engineer.pol.bosscreator.core.BossManager;
import engineer.pol.bosscreator.file.DataFile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class BossCreator implements ModInitializer {

    public static final String MOD_ID = "bosscreator";
    public static MinecraftServer SERVER = null;
    public static DataFile DATA_FILE = null;
    public static BossManager BOSS_MANAGER = null;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(BossCreatorCommand::register);
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            if (SERVER == null) {
                SERVER = server;
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> BOSS_MANAGER.save());

        DATA_FILE = new DataFile();
        BOSS_MANAGER = new BossManager();
    }
}
