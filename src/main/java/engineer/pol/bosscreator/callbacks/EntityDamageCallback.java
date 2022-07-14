package engineer.pol.bosscreator.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;

public interface EntityDamageCallback {

    Event<EntityDamageCallback> EVENT = EventFactory.createArrayBacked(EntityDamageCallback.class,
        (listeners) -> (receiver, source, amount) -> {
            for (EntityDamageCallback listener : listeners) {
                Pair<ActionResult, Float> result = listener.onDamage(receiver, source, amount);

                if (result.getLeft() != ActionResult.PASS) {
                    return result;
                }
            }
            return new Pair<>(ActionResult.PASS, amount);
        });

    Pair<ActionResult, Float> onDamage(LivingEntity receiver, EntityDamageSource source, float amount);


}
