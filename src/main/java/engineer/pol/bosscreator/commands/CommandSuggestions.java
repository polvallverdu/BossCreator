package engineer.pol.bosscreator.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import engineer.pol.bosscreator.FightCreator;
import engineer.pol.bosscreator.utils.BossBarUtils;
import net.minecraft.server.command.ServerCommandSource;

public class CommandSuggestions {

    public static SuggestionProvider<ServerCommandSource> CONFIGS = (context, builder) -> {
        FightCreator.FIGHT_MANAGER.getBossTemplates().forEach(bossTemplate -> builder.suggest(bossTemplate.getName()));
        return builder.buildFuture();
    };

    public static SuggestionProvider<ServerCommandSource> FIGHTS = (context, builder) -> {
        FightCreator.FIGHT_MANAGER.getFights().forEach(fight -> builder.suggest(fight.getName()));
        return builder.buildFuture();
    };

    public static SuggestionProvider<ServerCommandSource> BOSSBAR_COLORS = (context, builder) -> {
        BossBarUtils.getBossbarColors().forEach(builder::suggest);
        return builder.buildFuture();
    };

}
