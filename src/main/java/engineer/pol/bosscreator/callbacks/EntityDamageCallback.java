package engineer.pol.bosscreator.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.util.ActionResult;

public interface EntityDamageCallback {

    Event<EntityDamageCallback> EVENT = EventFactory.createArrayBacked(EntityDamageCallback.class,
        (listeners) -> (receiver, source, amount) -> {
            for (EntityDamageCallback listener : listeners) {
                ActionResult result = listener.onDamage(receiver, source, amount);

                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            return ActionResult.PASS;
        });

    ActionResult onDamage(LivingEntity receiver, EntityDamageSource source, float amount);


}
