package engineer.pol.bosscreator.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @ModifyConstant(method = "damage", constant = @Constant(floatValue = 0.0f, ordinal = 1))
    private float bypassZeroAmountCheck(float constant) {
        return -99f;
    }
}
