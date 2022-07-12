package engineer.pol.bosscreator.mixin;

import engineer.pol.bosscreator.callbacks.EntityDamageCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class EntityReceiveDamageMixin {

    @Inject(at = @At("HEAD"), method = "damage", cancellable = true)
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!(source instanceof EntityDamageSource)) return;
        EntityDamageSource entityDamageSource = (EntityDamageSource) source;

        ActionResult result = EntityDamageCallback.EVENT.invoker().onDamage(
            (LivingEntity) (Object) this, entityDamageSource, amount
        );

        if (result != ActionResult.PASS) {
            cir.setReturnValue(result == ActionResult.SUCCESS);
        }
    }

}
