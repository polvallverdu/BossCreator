package engineer.pol.bosscreator.mixin;

import engineer.pol.bosscreator.FightCreator;
import engineer.pol.bosscreator.models.BossFight;
import engineer.pol.bosscreator.models.FightType;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ScaleUtils.class)
public class PehkuiMixin {

    @Inject(method = "getKnockbackScale(Lnet/minecraft/entity/Entity;)F", at = @At("HEAD"), cancellable = true)
    private static void onPehkuiGetKnockback(Entity entity, CallbackInfoReturnable<Float> cir) {
        if (!(entity instanceof ServerPlayerEntity) || !FightCreator.FIGHT_MANAGER.isKnockback()) return;

        if (FightCreator.FIGHT_MANAGER.getActiveFights(FightType.BOSSFIGHT).stream().anyMatch(f -> ((BossFight) f).hasMorphedPlayer(entity.getUuid()))) {
            cir.setReturnValue(0F);
        }
    }

}
