package engineer.pol.bosscreator;

import engineer.pol.bosscreator.commands.FightCommand;
import engineer.pol.bosscreator.core.FightManager;
import engineer.pol.bosscreator.utils.cmds.CmdManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class FightCreator implements ModInitializer {

    public static final String MOD_ID = "bosscreator";
    public static MinecraftServer SERVER = null;
    public static FightManager FIGHT_MANAGER = null;
    public static CmdManager CMD_MANAGER = null;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(FightCommand::register);
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            if (SERVER == null) {
                SERVER = server;
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            FIGHT_MANAGER.save();
            CMD_MANAGER.save();
        });

        FIGHT_MANAGER = new FightManager();
        CMD_MANAGER = new CmdManager();
    }
}
