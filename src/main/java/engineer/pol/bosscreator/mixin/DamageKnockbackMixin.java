package engineer.pol.bosscreator.mixin;


import engineer.pol.bosscreator.FightCreator;
import engineer.pol.bosscreator.models.BossFight;
import engineer.pol.bosscreator.models.FightType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(LivingEntity.class)
public abstract class DamageKnockbackMixin extends Entity {

    public DamageKnockbackMixin(EntityType<?> type, World world) {
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


}
