package engineer.pol.bosscreator.mixin;

import com.mojang.authlib.GameProfile;
import engineer.pol.bosscreator.callbacks.EntityDamageCallback;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @ModifyArgs(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private void injected(Args args) {
        DamageSource source = args.get(0);
        float amount = args.get(1);

        if (!(source instanceof EntityDamageSource)) return;
        EntityDamageSource entityDamageSource = (EntityDamageSource) source;

        Pair<ActionResult, Float> result = EntityDamageCallback.EVENT.invoker().onDamage(
                (LivingEntity) (Object) this, entityDamageSource, amount, this.calculateDamage(entityDamageSource, amount)
        );

        if (result.getLeft() != ActionResult.PASS) {
            args.set(1, result.getRight());
        }
    }

    private float calculateDamage(DamageSource source, float amount) {
        amount = DamageUtil.getDamageLeft(amount, this.getArmor(), (float)this.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
        amount = this.modifyAppliedDamage(source, amount);

        return amount;
    }

}
