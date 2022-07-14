package engineer.pol.bosscreator.mixin;

import engineer.pol.bosscreator.callbacks.EntityDamageCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ServerPlayerEntity.class)
public class PlayerReceiveDamageMixin {

    @ModifyArgs(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private void injected(Args args) {
        DamageSource source = args.get(0);
        float amount = args.get(1);

        if (!(source instanceof EntityDamageSource)) return;
        EntityDamageSource entityDamageSource = (EntityDamageSource) source;

        Pair<ActionResult, Float> result = EntityDamageCallback.EVENT.invoker().onDamage(
                (LivingEntity) (Object) this, entityDamageSource, amount
        );

        if (result.getLeft() != ActionResult.PASS) {
            args.set(1, result.getRight());
        }
    }

}
