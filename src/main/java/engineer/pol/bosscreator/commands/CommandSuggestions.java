package engineer.pol.bosscreator.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import engineer.pol.bosscreator.BossCreator;
import engineer.pol.bosscreator.utils.BossBarUtils;
import net.minecraft.server.command.ServerCommandSource;

public class CommandSuggestions {

    public static SuggestionProvider<ServerCommandSource> BOSS_TEMPLATES = (context, builder) -> {
        BossCreator.BOSS_MANAGER.getBossTemplates().forEach(bossTemplate -> builder.suggest(bossTemplate.getName()));
        return builder.buildFuture();
    };

    public static SuggestionProvider<ServerCommandSource> BOSS_FIGHTS = (context, builder) -> {
        BossCreator.BOSS_MANAGER.getBossFights().forEach(fight -> builder.suggest(fight.getName()));
        return builder.buildFuture();
    };

    public static SuggestionProvider<ServerCommandSource> BOSSBAR_COLORS = (context, builder) -> {
        BossBarUtils.getBossbarColors().forEach(builder::suggest);
        return builder.buildFuture();
    };

}
