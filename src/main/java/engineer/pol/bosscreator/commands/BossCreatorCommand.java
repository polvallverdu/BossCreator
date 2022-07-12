package engineer.pol.bosscreator.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import engineer.pol.bosscreator.BossCreator;
import engineer.pol.bosscreator.models.BossFight;
import engineer.pol.bosscreator.models.BossTemplate;
import engineer.pol.bosscreator.utils.BossBarUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BossCreatorCommand {

    private static String prefix = "§8[§4BossCreator§8]§r ";

    private static String helpCommand = prefix + "§6List of commands: \n\n" +
            "§6/bosses help §8- §7Shows this message\n" +
            "§6/bosses list §8- §7Shows a list of all loaded templates\n" +
            "§6/bosses create <name> §8- §7Creates a boss template\n" +
            "§6/bosses remove <name> §8- §7Removes boss template\n" +
            "§6/bosses save §8- §7Saves all changes made\n" +
            "§6/bosses melee <name> <value> §8- §7Change damage made by boss\n" +
            "§6/bosses bossbar <name> <color> §8- §7Change color of bossbar\n" +
            "§6/bosses maxHealth <name> <value> §8- §7Change maxHealth of boss\n" +
            "§6/bosses displayName <name> <displayName> §8- §7Change displayName\n" +
            "§6/bosses start <name> <bossfightName> §8- §7Start/create a bossfight\n" +
            "§6/bosses finish <bossfightName> §8- §7Stop a bossfight\n" +
            "§6/bosses set <player> <bossfightName> §8- §7Sets a player\n" +
            "§6/bosses unset <player> <bossfightName> §8- §7Unsets a player\n" +
            "§6/bosses health <bossfightName> <value 0-100%> §8- §7Change boss health\n";


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        LiteralArgumentBuilder<ServerCommandSource> literalBuilder = CommandManager.literal("bosses");

        literalBuilder.executes(BossCreatorCommand::help);
        literalBuilder.then(CommandManager.literal("help").executes(BossCreatorCommand::help));

        literalBuilder.then(CommandManager.literal("create").then(CommandManager.argument("name", StringArgumentType.word()).executes(BossCreatorCommand::create)));
        literalBuilder.then(CommandManager.literal("remove").then(CommandManager.argument("name", StringArgumentType.word()).suggests(CommandSuggestions.BOSS_TEMPLATES).executes(BossCreatorCommand::remove)));
        literalBuilder.then(CommandManager.literal("list").executes(BossCreatorCommand::list));
        literalBuilder.then(CommandManager.literal("save").executes(BossCreatorCommand::save));

        literalBuilder
                .then(CommandManager.literal("melee")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.BOSS_TEMPLATES)
                                .then(CommandManager.argument("meleeDamage", IntegerArgumentType.integer(0))
                                        .executes(BossCreatorCommand::setMelee))));

        LiteralArgumentBuilder<ServerCommandSource> bossbarBuilder = literalBuilder
                .then(CommandManager.literal("bossbar")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.BOSS_TEMPLATES)));
        BossBarUtils.getBossbarColors().forEach(color -> bossbarBuilder.then(CommandManager.literal(color).executes(context -> setBossbarColor(context, color))));

        literalBuilder
                .then(CommandManager.literal("maxHealth")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.BOSS_TEMPLATES)
                                .then(CommandManager.argument("maxHealth", IntegerArgumentType.integer(0))
                                        .executes(BossCreatorCommand::setMaxHealth))));

        literalBuilder
                .then(CommandManager.literal("displayName")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.BOSS_TEMPLATES)
                                .then(CommandManager.argument("displayName",StringArgumentType.string())
                                        .executes(BossCreatorCommand::setDisplayName))));

        literalBuilder
                .then(CommandManager.literal("start")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests(CommandSuggestions.BOSS_TEMPLATES)
                                .then(CommandManager.argument("bossfightName",StringArgumentType.word())
                                        .executes(BossCreatorCommand::start))));

        literalBuilder
                .then(CommandManager.literal("finish")
                        .then(CommandManager.argument("bossfightName", StringArgumentType.word())
                                .suggests(CommandSuggestions.BOSS_FIGHTS)
                                .executes(BossCreatorCommand::finish)));

        literalBuilder
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("bossfightName", StringArgumentType.word())
                                .suggests(CommandSuggestions.BOSS_FIGHTS)
                                .then(CommandManager.argument("playerName", EntityArgumentType.player())
                                        .executes(BossCreatorCommand::setPlayer))));

        literalBuilder
                .then(CommandManager.literal("unset")
                        .then(CommandManager.argument("bossfightName", StringArgumentType.word())
                                .suggests(CommandSuggestions.BOSS_FIGHTS)
                                .then(CommandManager.argument("playerName", EntityArgumentType.player())
                                        .executes(BossCreatorCommand::unsetPlayer))));

        literalBuilder
                .then(CommandManager.literal("health")
                        .then(CommandManager.argument("bossfightName", StringArgumentType.word())
                                .suggests(CommandSuggestions.BOSS_FIGHTS)
                                .then(CommandManager.argument("health", IntegerArgumentType.integer(0, 100))
                                        .executes(BossCreatorCommand::setHealth))));

        dispatcher.register(literalBuilder);
    }

    private static int help(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(Text.literal(helpCommand), false);
        return 1;
    }

    private static int create(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        BossTemplate bossTemplate;
        try {
            bossTemplate = BossCreator.BOSS_MANAGER.createBossTemplate(name);
        } catch (RuntimeException e) {
            context.getSource().sendFeedback(Text.literal(e.getMessage()), false);
            return 1;
        }
        context.getSource().sendFeedback(Text.literal("Boss template created: " + bossTemplate.getName()), false);
        return 1;
    }

    private static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        BossTemplate bossTemplate;
        try {
            bossTemplate = BossCreator.BOSS_MANAGER.removeBossTemplate(name);
        } catch (RuntimeException e) {
            context.getSource().sendFeedback(Text.literal(e.getMessage()), false);
            return 1;
        }
        context.getSource().sendFeedback(Text.literal("Boss template removed: " + bossTemplate.getName()), false);
        return 1;
    }

    private static int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("§7List of bosses: ");
        for (BossTemplate bossTemplate : BossCreator.BOSS_MANAGER.getBossTemplates()) {
            sb.append("§6").append(bossTemplate.getName()).append("§8, ");
        }
        context.getSource().sendFeedback(Text.literal(sb.toString()), false);
        return 1;
    }

    private static int save(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BossCreator.BOSS_MANAGER.save();
        context.getSource().sendFeedback(Text.literal("Bosses saved"), false);
        return 1;
    }

    private static int setMelee(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        int meleeDamage = IntegerArgumentType.getInteger(context, "meleeDamage");
        BossTemplate bossTemplate = BossCreator.BOSS_MANAGER.getBossTemplate(name);;
        if (bossTemplate == null) {
            context.getSource().sendFeedback(Text.literal("Boss template not found"), false);
            return 1;
        }

        bossTemplate.setMeleeDamage(meleeDamage);
        context.getSource().sendFeedback(Text.literal("Melee damage updated"), false);
        return 1;
    }

    private static int setBossbarColor(CommandContext<ServerCommandSource> context, String color) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        BossBar.Color bossbarColor = BossBarUtils.getColorFromString(color);

        if (bossbarColor == null) {
            context.getSource().sendFeedback(Text.literal("Invalid bossbar color"), false);
            return 1;
        }

        BossTemplate bossTemplate = BossCreator.BOSS_MANAGER.getBossTemplate(name);
        if (bossTemplate == null) {
            context.getSource().sendFeedback(Text.literal("Boss template not found"), false);
            return 1;
        }

        bossTemplate.setColor(bossbarColor);
        context.getSource().sendFeedback(Text.literal("Bossbar color updated"), false);
        return 1;
    }

    private static int setMaxHealth(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        int maxHealth = IntegerArgumentType.getInteger(context, "maxHealth");
        BossTemplate bossTemplate = BossCreator.BOSS_MANAGER.getBossTemplate(name);
        if (bossTemplate == null) {
            context.getSource().sendFeedback(Text.literal("Invalid boss name"), false);
            return 1;
        }

        bossTemplate.setMaxHealth(maxHealth);
        context.getSource().sendFeedback(Text.literal("Max health updated"), false);
        return 1;
    }

    private static int setDisplayName(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        String displayName = StringArgumentType.getString(context, "displayName");
        BossTemplate bossTemplate = BossCreator.BOSS_MANAGER.getBossTemplate(name);
        if (bossTemplate == null) {
            context.getSource().sendFeedback(Text.literal("Boss not found"), false);
            return 1;
        }

        bossTemplate.setDisplayName(displayName);
        context.getSource().sendFeedback(Text.literal("Display name updated"), false);
        return 1;
    }

    private static int start(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        String bossfightName = StringArgumentType.getString(context, "bossfightName");
        BossTemplate bossTemplate = BossCreator.BOSS_MANAGER.getBossTemplate(name);
        if (bossTemplate == null) {
            context.getSource().sendFeedback(Text.literal("Boss template not found"), false);
            return 1;
        }

        BossFight bossFight = BossCreator.BOSS_MANAGER.createBossFight(bossfightName, bossTemplate);
        bossFight.start();

        context.getSource().sendFeedback(Text.literal("Boss started"), false);
        return 1;
    }

    private static int finish(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String bossfightName = StringArgumentType.getString(context, "bossfightName");
        BossFight bossFight = BossCreator.BOSS_MANAGER.getBossFight(bossfightName);
        if (bossFight == null) {
            context.getSource().sendFeedback(Text.literal("Bossfight not found"), false);
            return 1;
        }

        bossFight.stop();
        context.getSource().sendFeedback(Text.literal("Boss finished"), false);
        return 1;
    }

    private static int setPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String bossfightName = StringArgumentType.getString(context, "bossfightName");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "playerName");
        BossFight bossFight = BossCreator.BOSS_MANAGER.getBossFight(bossfightName);
        if (bossFight == null) {
            context.getSource().sendFeedback(Text.literal("Bossfight not found"), false);
            return 1;
        }

        bossFight.addMorphedPlayer(player.getUuid());
        context.getSource().sendFeedback(Text.literal("Player set"), false);
        return 1;
    }

    private static int unsetPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String bossfightName = StringArgumentType.getString(context, "bossfightName");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "playerName");
        BossFight bossFight = BossCreator.BOSS_MANAGER.getBossFight(bossfightName);
        if (bossFight == null) {
            context.getSource().sendFeedback(Text.literal("Bossfight not found"), false);
            return 1;
        }

        bossFight.removeMorpedPlayer(player.getUuid());
        context.getSource().sendFeedback(Text.literal("Player unset"), false);
        return 1;
    }

    private static int setHealth(CommandContext<ServerCommandSource> context) {
        String bossfightName = StringArgumentType.getString(context, "bossfightName");
        int health = IntegerArgumentType.getInteger(context, "health");
        float percentage = (float) health/100;
        BossFight bossFight = BossCreator.BOSS_MANAGER.getBossFight(bossfightName);
        if (bossFight == null) {
            context.getSource().sendFeedback(Text.literal("Bossfight not found"), false);
            return 1;
        }

        bossFight.setHP(percentage);

        return 1;
    }

}
