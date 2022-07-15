package engineer.pol.bosscreator.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import engineer.pol.bosscreator.FightCreator;
import engineer.pol.bosscreator.models.BossFight;
import engineer.pol.bosscreator.models.Fight;
import engineer.pol.bosscreator.models.FightConfig;
import engineer.pol.bosscreator.models.PlayerFight;
import engineer.pol.bosscreator.utils.BossBarUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class FightCommand {

    public static String PREFIX = "§8[§4FightCreator§8]§r ";

    private static String helpCommand = PREFIX + "§6List of commands: \n\n" +
            "§6/fight help §8- §7Shows this message\n" +
            "§6/fight list §8- §7Shows a list of all loaded templates\n" +
            "§6/fight createConfig <configName> §8- §7Creates a boss template\n" +
            "§6/fight createBossFight <configName> <fightName> §8- §7Creates a boss fight\n" +
            "§6/fight createPlayerFight <configName> <fightName> §8- §7Creates a player fight\n" +
            "§6/fight remove <name> §8- §7Removes a config\n" +
            "§6/fight save §8- §7Saves all changes made\n" +
            "§6/fight setMelee <name> <value> §8- §7Change damage made by a melee attack\n" +
            "§6/fight setProjectile <name> <value> §8- §7Change damage made by a projectile\n" +
            "§6/fight setBossbarColor <name> <color> §8- §7Change color of bossbar\n" +
            "§6/fight setMaxHealth <name> <value> §8- §7Change maxHealth of boss\n" +
            "§6/fight setDisplayName <name> <displayName> §8- §7Change displayName\n" +
            "§6/fight start <fightName> §8- §7Start a fight\n" +
            "§6/fight finish <fightName> §8- §7Stop a fight\n" +
            "§6/fight set <player> <fightName> §8- §7Sets a player\n" +
            "§6/fight unset <player> <fightName> §8- §7Unsets a player\n" +
            "§6/fight setKnockback <true/false> §8- §7Enables knokback for fights\n" +
            "§6/fight modifyHealth <fightName> <value 0-100%> §8- §7Change boss health\n" +
            "§6/fight blockLimit <limit 0-100%> §8- §7Set the limit for the bossfight\n" +
            "§6/fight blockEnabled <true/false> §8- §7Sets if the percentage block will trigger\n" +
            "§6/fight block <true/false> §8- §7Block fight bossbar";


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        LiteralArgumentBuilder<ServerCommandSource> literalBuilder = CommandManager.literal("fight");

        literalBuilder.executes(FightCommand::help);
        literalBuilder.then(CommandManager.literal("help").executes(FightCommand::help));

        literalBuilder.then(CommandManager.literal("createConfig").then(CommandManager.argument("name", StringArgumentType.word()).executes(FightCommand::create)));
        literalBuilder.then(CommandManager.literal("removeConfig").then(CommandManager.argument("name", StringArgumentType.word()).suggests(CommandSuggestions.CONFIGS).executes(FightCommand::remove)));
        literalBuilder.then(CommandManager.literal("list").executes(FightCommand::list));
        literalBuilder.then(CommandManager.literal("save").executes(FightCommand::save));

        literalBuilder
                .then(CommandManager.literal("setMelee")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.CONFIGS)
                                .then(CommandManager.argument("meleeDamage", IntegerArgumentType.integer(-1))
                                        .executes(FightCommand::setMeleeDamage))));

        literalBuilder
                .then(CommandManager.literal("setProjectile")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.CONFIGS)
                                .then(CommandManager.argument("projectileDamage", IntegerArgumentType.integer(-1))
                                        .executes(FightCommand::setProjectileDamage))));

        literalBuilder
                .then(CommandManager.literal("setBossbarColor")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.CONFIGS)
                        .then(CommandManager.argument("color", StringArgumentType.word())
                                .suggests(CommandSuggestions.BOSSBAR_COLORS)
                                .executes(FightCommand::setBossbarColor))));

        literalBuilder
                .then(CommandManager.literal("setMaxHealth")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.CONFIGS)
                                .then(CommandManager.argument("maxHealth", IntegerArgumentType.integer(0))
                                        .executes(FightCommand::setMaxHealth))));

        literalBuilder
                .then(CommandManager.literal("setDisplayName")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.CONFIGS)
                                .then(CommandManager.argument("displayName",StringArgumentType.string())
                                        .executes(FightCommand::setDisplayName))));

        literalBuilder
                .then(CommandManager.literal("createBossFight")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.CONFIGS)
                                .then(CommandManager.argument("fightName",StringArgumentType.word())
                                        .executes(FightCommand::createBossfight))));

        literalBuilder
                .then(CommandManager.literal("createPlayerFight")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.CONFIGS)
                                .then(CommandManager.argument("fightName",StringArgumentType.word())
                                        .executes(FightCommand::createPlayerfight))));

        literalBuilder
                .then(CommandManager.literal("start")
                        .then(CommandManager.argument("fightName", StringArgumentType.word())
                                .suggests(CommandSuggestions.FIGHTS)
                                .executes(FightCommand::start)));

        literalBuilder
                .then(CommandManager.literal("finish")
                        .then(CommandManager.argument("fightName", StringArgumentType.word())
                                .suggests(CommandSuggestions.FIGHTS)
                                .executes(FightCommand::finish)));

        literalBuilder
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("fightName", StringArgumentType.word())
                                .suggests(CommandSuggestions.FIGHTS)
                                .then(CommandManager.argument("playerName", EntityArgumentType.player())
                                        .executes(FightCommand::setPlayer))));

        literalBuilder
                .then(CommandManager.literal("unset")
                        .then(CommandManager.argument("fightName", StringArgumentType.word())
                                .suggests(CommandSuggestions.FIGHTS)
                                .then(CommandManager.argument("playerName", EntityArgumentType.player())
                                        .executes(FightCommand::unsetPlayer))));

        literalBuilder
                .then(CommandManager.literal("modifyHealth")
                        .then(CommandManager.argument("fightName", StringArgumentType.word())
                                .suggests(CommandSuggestions.FIGHTS)
                                .then(CommandManager.argument("health", IntegerArgumentType.integer(0, 100))
                                        .executes(FightCommand::setHealth))));

        literalBuilder
                .then(CommandManager.literal("setKnockback")
                        .then(CommandManager.argument("knockback", BoolArgumentType.bool())
                                        .executes(FightCommand::setKnockback)));

        LiteralArgumentBuilder<ServerCommandSource> cmdsBuilder = CommandManager.literal("cmds");
        CmdSubcommand.register(cmdsBuilder);
        literalBuilder.then(cmdsBuilder);

        literalBuilder.then(CommandManager.literal("blockLimit").then(CommandManager.argument("limit", IntegerArgumentType.integer(0, 100)).executes(FightCommand::setBlockLimit)));
        literalBuilder.then(CommandManager.literal("blockTrigger").then(CommandManager.argument("value", BoolArgumentType.bool()).executes(FightCommand::setBlockEnabled)));
        literalBuilder.then(CommandManager.literal("block").then(CommandManager.argument("value", BoolArgumentType.bool()).executes(FightCommand::setBlock)));

        dispatcher.register(literalBuilder);
    }

    private static int help(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(Text.literal(helpCommand), false);
        return 1;
    }

    private static int create(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        FightConfig fightConfig;
        try {
            fightConfig = FightCreator.FIGHT_MANAGER.createBossTemplate(name);
        } catch (RuntimeException e) {
            context.getSource().sendFeedback(Text.literal(e.getMessage()), false);
            return 1;
        }
        context.getSource().sendFeedback(Text.literal("Boss template created: " + fightConfig.getName()), false);
        return 1;
    }

    private static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        FightConfig fightConfig;
        try {
            fightConfig = FightCreator.FIGHT_MANAGER.removeBossTemplate(name);
        } catch (RuntimeException e) {
            context.getSource().sendFeedback(Text.literal(e.getMessage()), false);
            return 1;
        }
        context.getSource().sendFeedback(Text.literal("Boss template removed: " + fightConfig.getName()), false);
        return 1;
    }

    private static int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX).append("§7List of fight: ");
        for (FightConfig fightConfig : FightCreator.FIGHT_MANAGER.getBossTemplates()) {
            sb.append("§6").append(fightConfig.getName()).append("§8, ");
        }
        context.getSource().sendFeedback(Text.literal(sb.toString()), false);
        return 1;
    }

    private static int save(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        FightCreator.FIGHT_MANAGER.save();
        FightCreator.CMD_MANAGER.save();
        context.getSource().sendFeedback(Text.literal(PREFIX + "Config saved"), false);
        return 1;
    }

    private static int setMeleeDamage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        int meleeDamage = IntegerArgumentType.getInteger(context, "meleeDamage");
        FightConfig fightConfig = FightCreator.FIGHT_MANAGER.getFightConfig(name);
        if (fightConfig == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Boss template not found"), false);
            return 1;
        }

        fightConfig.setMeleeDamage(meleeDamage);
        context.getSource().sendFeedback(Text.literal(PREFIX + "Melee damage updated"), false);
        save();
        return 1;
    }

    private static int setProjectileDamage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        int projectileDamage = IntegerArgumentType.getInteger(context, "projectileDamage");
        FightConfig fightConfig = FightCreator.FIGHT_MANAGER.getFightConfig(name);
        if (fightConfig == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Boss template not found"), false);
            return 1;
        }

        fightConfig.setProjectileDamage(projectileDamage);
        context.getSource().sendFeedback(Text.literal(PREFIX + "Projectile damage updated"), false);
        save();
        return 1;
    }

    private static int setBossbarColor(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        BossBar.Color bossbarColor = BossBarUtils.getColorFromString(StringArgumentType.getString(context, "color"));

        if (bossbarColor == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Invalid bossbar color"), false);
            return 1;
        }

        FightConfig fightConfig = FightCreator.FIGHT_MANAGER.getFightConfig(name);
        if (fightConfig == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Boss template not found"), false);
            return 1;
        }

        fightConfig.setColor(bossbarColor);
        context.getSource().sendFeedback(Text.literal(PREFIX + "Bossbar color updated"), false);
        save();
        return 1;
    }

    private static int setMaxHealth(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        int maxHealth = IntegerArgumentType.getInteger(context, "maxHealth");
        FightConfig fightConfig = FightCreator.FIGHT_MANAGER.getFightConfig(name);
        if (fightConfig == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Invalid boss name"), false);
            return 1;
        }

        fightConfig.setMaxHealth(maxHealth);
        context.getSource().sendFeedback(Text.literal(PREFIX + "Max health updated"), false);
        save();
        return 1;
    }

    private static int setDisplayName(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        String displayName = StringArgumentType.getString(context, "displayName");
        FightConfig fightConfig = FightCreator.FIGHT_MANAGER.getFightConfig(name);
        if (fightConfig == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Boss not found"), false);
            return 1;
        }

        fightConfig.setDisplayName(displayName);
        context.getSource().sendFeedback(Text.literal(PREFIX + "Display name updated"), false);
        save();
        return 1;
    }

    private static int createBossfight(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        String fightName = StringArgumentType.getString(context, "fightName");
        FightConfig fightConfig = FightCreator.FIGHT_MANAGER.getFightConfig(name);
        if (fightConfig == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Config not found"), false);
            return 1;
        }

        try {
            BossFight bossFight = FightCreator.FIGHT_MANAGER.createBossFight(fightName, fightConfig);
        } catch (RuntimeException e) {
            context.getSource().sendFeedback(Text.literal(e.getMessage()), false);
            return 1;
        }

        context.getSource().sendFeedback(Text.literal(PREFIX + "Bossfight created"), false);
        return 1;
    }

    private static int createPlayerfight(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        String fightName = StringArgumentType.getString(context, "fightName");
        FightConfig fightConfig = FightCreator.FIGHT_MANAGER.getFightConfig(name);
        if (fightConfig == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Config not found"), false);
            return 1;
        }

        try {
            PlayerFight playerFight = FightCreator.FIGHT_MANAGER.createPlayerFight(fightName, fightConfig);
        } catch (RuntimeException e) {
            context.getSource().sendFeedback(Text.literal(e.getMessage()), false);
            return 1;
        }

        context.getSource().sendFeedback(Text.literal(PREFIX + "Playerfight created"), false);
        return 1;
    }

    private static int start(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String fightName = StringArgumentType.getString(context, "fightName");
        Fight fight = FightCreator.FIGHT_MANAGER.getFight(fightName);
        if (fight == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Boss fight not found"), false);
            return 1;
        }

        if (fight.isRunning()) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Boss fight is already running"), false);
            return 1;
        }
        fight.start();
        context.getSource().sendFeedback(Text.literal(PREFIX + "Boss started"), false);
        return 1;
    }

    private static int finish(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String fightName = StringArgumentType.getString(context, "fightName");
        Fight fight = FightCreator.FIGHT_MANAGER.getFight(fightName);
        if (fight == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Fight not found"), false);
            return 1;
        }

        if (!fight.isRunning()) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Fight is not running"), false);
            return 1;
        }
        fight.stop(true);
        context.getSource().sendFeedback(Text.literal(PREFIX + "Fight finished"), false);
        return 1;
    }

    private static int setPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String fightName = StringArgumentType.getString(context, "fightName");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "playerName");
        Fight fight = FightCreator.FIGHT_MANAGER.getFight(fightName);
        if (fight == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Bossfight not found"), false);
            return 1;
        }

        if (fight instanceof BossFight) {
            ((BossFight) fight).addMorphedPlayer(player.getUuid());
        } else if (fight instanceof PlayerFight) {
            ((PlayerFight) fight).setPlayer(player);
        }

        context.getSource().sendFeedback(Text.literal(PREFIX + "Player set"), false);
        return 1;
    }

    private static int unsetPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String fightName = StringArgumentType.getString(context, "fightName");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "playerName");
        Fight fight = FightCreator.FIGHT_MANAGER.getFight(fightName);
        if (fight == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Bossfight not found"), false);
            return 1;
        }

        if (fight instanceof BossFight) {
            ((BossFight) fight).removeMorphedPlayer(player.getUuid());
        } else if (fight instanceof PlayerFight) {
            ((PlayerFight) fight).setPlayer(null);
        }

        context.getSource().sendFeedback(Text.literal(PREFIX + "Player unset"), false);
        return 1;
    }

    private static int setHealth(CommandContext<ServerCommandSource> context) {
        String fightName = StringArgumentType.getString(context, "fightName");
        int health = IntegerArgumentType.getInteger(context, "health");
        float percentage = (float) health/100;
        Fight fight = FightCreator.FIGHT_MANAGER.getFight(fightName);
        if (fight == null) {
            context.getSource().sendFeedback(Text.literal(PREFIX + "Bossfight not found"), false);
            return 1;
        }

        if (percentage > 1) {
            percentage = 1;
        } else if (percentage < 0) {
            percentage = 0;
        }

        if (fight instanceof BossFight) {
            ((BossFight) fight).setHP(percentage);
        } else if (fight instanceof PlayerFight) {
            ((PlayerFight) fight).setHP(percentage);
        }

        context.getSource().sendFeedback(Text.literal(PREFIX + "Updated bar"), false);
        return 1;
    }

    private static int setKnockback(CommandContext<ServerCommandSource> context) {
        FightCreator.FIGHT_MANAGER.setKnockback(BoolArgumentType.getBool(context, "knockback"));

        context.getSource().sendFeedback(Text.literal(PREFIX + (FightCreator.FIGHT_MANAGER.isKnockback() ? "§aSet knockback to true" : "§aSet knockback to false")), false);

        return 1;
    }

    private static int setBlockLimit(CommandContext<ServerCommandSource> context) {
        int percentage = IntegerArgumentType.getInteger(context, "limit");
        FightCreator.FIGHT_MANAGER.setBlockPercentage(percentage);

        context.getSource().sendFeedback(Text.literal(PREFIX + "Set block limit to " + percentage), false);

        return 1;
    }

    private static int setBlockEnabled(CommandContext<ServerCommandSource> context) {
        boolean blockEnabled = BoolArgumentType.getBool(context, "value");
        FightCreator.FIGHT_MANAGER.setBlockEnabled(blockEnabled);

        context.getSource().sendFeedback(Text.literal(PREFIX + (blockEnabled ? "§aSet block enabled to true" : "§aSet block enabled to false")), false);
        return 1;
    }

    private static int setBlock(CommandContext<ServerCommandSource> context) {
        boolean block = BoolArgumentType.getBool(context, "value");

        FightCreator.FIGHT_MANAGER.setBlocked(block);

        context.getSource().sendFeedback(Text.literal(PREFIX + (block ? "§aSet block to true" : "§aSet block to false")), false);
        return 1;
    }

    private static void save() {
        FightCreator.FIGHT_MANAGER.save();
    }

}
