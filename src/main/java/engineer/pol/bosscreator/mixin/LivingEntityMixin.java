package engineer.pol.bosscreator.mixin;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /*@ModifyConstant(method = "damage", constant = @Constant(doubleValue = 0.4f))
    private double modifyDamageKnockback(double value, DamageSource source, float amount) {
        return check(value);
    }

    @ModifyConstant(method = "knockback(Lnet/minecraft/entity/LivingEntity;)V", constant = @Constant(doubleValue = 0.5D))
    private double modifyKnockback(double value, LivingEntity target) {
        return check(value);
    }

    private double check(double value) {
        AtomicReference<Double> newValue = new AtomicReference<>(value);

        if (!FightCreator.FIGHT_MANAGER.isKnockback()) {
            FightCreator.FIGHT_MANAGER.getActiveFights(FightType.BOSSFIGHT).forEach(bf -> {
                if (((BossFight) bf).getMorphedPlayers().contains(this.getUuid())) {
                    newValue.set(0.0D);
                }
            });
        }

        return newValue.get();
    }*/

    @ModifyConstant(method = "applyDamage", constant = @Constant(floatValue = 0.0f, ordinal = 2))
    private float bypassZeroAmountCheck(float constant) {
        return -99f;
    }


}
