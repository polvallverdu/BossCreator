package engineer.pol.bosscreator.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import engineer.pol.bosscreator.FightCreator;
import engineer.pol.bosscreator.utils.cmds.CmdCases;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public class CmdSubcommand {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
        for (CmdCases cmdCase : CmdCases.values()) {
            builder.then(CommandManager.literal(cmdCase.name())
                    .then(CommandManager.literal("list").executes(context -> list(context, cmdCase)))
                    .then(CommandManager.literal("empty").executes(context -> empty(context, cmdCase)))
                    .then(CommandManager.literal("add").then(CommandManager.argument("command", StringArgumentType.greedyString()).executes(context -> add(context, cmdCase))))
                    .then(CommandManager.literal("remove").then(CommandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> remove(context, cmdCase))))
                    .then(CommandManager.literal("edit").then(CommandManager.argument("index", IntegerArgumentType.integer(0)).then(CommandManager.argument("command", StringArgumentType.greedyString()).executes(context -> edit(context, cmdCase)))))
            );
        }
    }

    private static int list(CommandContext<ServerCommandSource> context, CmdCases cmdCase) throws CommandSyntaxException {
        StringBuilder message = new StringBuilder(FightCommand.PREFIX).append("§7").append(cmdCase.name()).append(" commands: ");
        List<String> commands = FightCreator.CMD_MANAGER.getCommands(cmdCase);
        for (int i = 0; i < commands.size(); i++) {
            message.append("\n§f§l(").append(i).append(") §f").append(commands.get(i).replaceAll("\\{", "§3§l{").replaceAll("}", "}§r"));
        }
        context.getSource().sendFeedback(Text.literal(message.toString()), false);
        return 1;
    }

    private static int add(CommandContext<ServerCommandSource> context, CmdCases cmdCase) throws CommandSyntaxException {
        String command = context.getArgument("command", String.class);

        List<String> commands = FightCreator.CMD_MANAGER.getCommands(cmdCase);
        commands.add(command);

        FightCreator.CMD_MANAGER.save();

        context.getSource().sendFeedback(Text.literal(FightCommand.PREFIX + "§f" + cmdCase.name() + " command added: §7" + command), false);
        return 1;
    }

    private static int remove(CommandContext<ServerCommandSource> context, CmdCases cmdCase) throws CommandSyntaxException {
        int index = context.getArgument("index", Integer.class);

        List<String> commands = FightCreator.CMD_MANAGER.getCommands(cmdCase);
        if (index >= commands.size()) {
            context.getSource().sendFeedback(Text.literal(FightCommand.PREFIX + "§f" + cmdCase.name() + " command not found"), false);
            return 1;
        }

        String cmd = commands.remove(index);

        FightCreator.CMD_MANAGER.save();

        context.getSource().sendFeedback(Text.literal(FightCommand.PREFIX + "§f" + cmdCase.name() + " command removed: §7" + cmd), false);
        return 1;
    }

    private static int edit(CommandContext<ServerCommandSource> context, CmdCases cmdCase) throws CommandSyntaxException {
        int index = context.getArgument("index", Integer.class);
        String command = context.getArgument("command", String.class);

        List<String> commands = FightCreator.CMD_MANAGER.getCommands(cmdCase);
        if (index >= commands.size()) {
            context.getSource().sendFeedback(Text.literal(FightCommand.PREFIX + "§f" + cmdCase.name() + " command not found"), false);
            return 1;
        }

        commands.set(index, command);

        FightCreator.CMD_MANAGER.save();

        context.getSource().sendFeedback(Text.literal(FightCommand.PREFIX + "§f" + cmdCase.name() + " command edited"), false);
        return 1;
    }

    private static int empty(CommandContext<ServerCommandSource> context, CmdCases cmdCase) throws CommandSyntaxException {
        List<String> commands = FightCreator.CMD_MANAGER.getCommands(cmdCase);

        commands.clear();

        FightCreator.CMD_MANAGER.save();

        context.getSource().sendFeedback(Text.literal(FightCommand.PREFIX + "§f" + cmdCase.name() + " case emptied"), false);
        return 1;
    }

}
